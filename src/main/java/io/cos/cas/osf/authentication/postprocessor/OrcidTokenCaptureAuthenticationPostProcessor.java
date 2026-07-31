package io.cos.cas.osf.authentication.postprocessor;

import io.cos.cas.osf.authentication.support.OrcidTokenRevocationClient;
import io.cos.cas.osf.dao.OsfOrcidTokenDao;
import io.cos.cas.osf.orcidtoken.OsfOrcidToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.profile.orcid.OrcidProfile;

/**
 * This is {@link OrcidTokenCaptureAuthenticationPostProcessor}.
 *
 * Captures the ORCID OAuth access token on a successful ORCID login (via pac4j delegated authentication) and stores
 * it in the writable {@code osf_orcid_oauth_token} table, so that OSF can later ask CAS to revoke it (see
 * {@code OrcidTokenRevocationController}, used on GDPR delete).
 *
 * <p><strong>Unverified assumption, pending a live spike:</strong> this relies on
 * {@link ClientCredential#getUserProfile()} already being populated (by CAS's pac4j-based authentication handling)
 * by the time {@link AuthenticationPostProcessor}s run for the transaction. This has been confirmed against the
 * compiled {@code ClientCredential} / {@code OAuth20Profile} API shapes (plain, nullable {@code getUserProfile()} /
 * {@code getAccessToken(): String} getters), but the exact point in the CAS 6.2.8 + pac4j 4.1.0 authentication
 * pipeline where {@code setUserProfile(...)} is actually invoked could not be confirmed via static inspection alone
 * (the relevant handler class is bundled only in the full CAS webapp WAR, not the thin support/api jars used to
 * develop this feature). If {@link #captureOrcidToken(ClientCredential)} logs the DEBUG "no resolved profile yet"
 * message on every real ORCID login, this hook needs to move to a different point in the pipeline (the fallback
 * discussed in the design doc is capturing inside {@code OsfPrincipalFromNonInteractiveCredentialsAction} instead,
 * though as currently written that class also runs before profile resolution and would need further changes).</p>
 *
 * <p>Wrapped entirely in try/catch: a failure here must never break a login.</p>
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OrcidTokenCaptureAuthenticationPostProcessor implements AuthenticationPostProcessor {

    private final String orcidClientName;

    private final String orcidClientId;

    private final String orcidClientSecret;

    private final String orcidRevokeUrl;

    private final OsfOrcidTokenDao osfOrcidTokenDao;

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof ClientCredential
                && orcidClientName.equalsIgnoreCase(((ClientCredential) credential).getClientName());
    }

    @Override
    public void process(
            final AuthenticationBuilder builder,
            final AuthenticationTransaction transaction
    ) throws AuthenticationException {
        transaction.getCredentials().stream()
                .filter(this::supports)
                .map(credential -> (ClientCredential) credential)
                .forEach(this::captureOrcidToken);
    }

    private void captureOrcidToken(final ClientCredential credential) {
        try {
            final CommonProfile profile = credential.getUserProfile();
            if (!(profile instanceof OrcidProfile)) {
                LOGGER.debug(
                        "No resolved ORCID profile on the client credential yet (profile=[{}]); "
                                + "skipping ORCID token capture for this authentication event.",
                        profile
                );
                return;
            }
            final OrcidProfile orcidProfile = (OrcidProfile) profile;
            final String orcidId = orcidProfile.getOrcid();
            final String accessToken = orcidProfile.getAccessToken();
            if (StringUtils.isBlank(orcidId) || StringUtils.isBlank(accessToken)) {
                LOGGER.warn("ORCID login resolved without an ORCID iD or access token; nothing to capture.");
                return;
            }
            final OsfOrcidToken existing = osfOrcidTokenDao.findByOrcidId(orcidId);
            if (existing != null
                    && StringUtils.isNotBlank(existing.getAccessToken())
                    && !existing.getAccessToken().equals(accessToken)) {
                LOGGER.debug("Reconnect detected for ORCID iD [{}]; revoking the previous token before replacing it.", orcidId);
                OrcidTokenRevocationClient.revoke(orcidRevokeUrl, orcidClientId, orcidClientSecret, existing.getAccessToken());
            }
            osfOrcidTokenDao.upsertToken(orcidId, accessToken, null, null);
            LOGGER.info("Captured ORCID OAuth token for ORCID iD [{}]", orcidId);
        } catch (final Exception e) {
            LOGGER.warn("Failed to capture ORCID OAuth token; login proceeds unaffected. Error: {}", e.getMessage());
            LOGGER.debug("Full stack trace of the ORCID token capture failure:", e);
        }
    }
}
