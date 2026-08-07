package io.cos.cas.osf.authentication.postprocessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.oauth.profile.orcid.OrcidProfile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OrcidTokenCaptureAuthenticationPostProcessor}.
 *
 * Captures the ORCID OAuth access token on a successful ORCID login (via pac4j delegated authentication) and attaches
 * it to the resolved principal's attributes ({@code orcidId}, {@code orcidAccessToken}), so it is released to OSF
 * through the same CAS attribute-release mechanism already used for {@code givenName} / {@code familyName} /
 * {@code username} (see e.g. {@code etc/cas/services/local/cas-203948234207100.json}). CAS does not persist the
 * token anywhere; OSF is responsible for storing it and, on GDPR delete, revoking it directly against ORCID.
 *
 * <p><strong>Unverified assumption, pending a live spike:</strong> this relies on
 * {@link ClientCredential#getUserProfile()} already being populated (by CAS's pac4j-based authentication handling)
 * and {@link AuthenticationBuilder#getPrincipal()} already holding the elected principal by the time
 * {@link AuthenticationPostProcessor}s run for the transaction. This has been confirmed against the compiled
 * {@code ClientCredential} / {@code OAuth20Profile} / {@code AuthenticationBuilder} API shapes, but the exact point
 * in the CAS 6.2.8 + pac4j 4.1.0 authentication pipeline where these are populated could not be confirmed via static
 * inspection alone (the relevant handler class is bundled only in the full CAS webapp WAR, not the thin support/api
 * jars used to develop this feature). If {@link #captureOrcidToken(ClientCredential, AuthenticationBuilder)} logs the
 * DEBUG "no resolved profile yet" or "no principal resolved yet" message on every real ORCID login, this hook needs
 * to move to a different point in the pipeline.</p>
 *
 * <p>Wrapped entirely in try/catch: a failure here must never break a login.</p>
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OrcidTokenCaptureAuthenticationPostProcessor implements AuthenticationPostProcessor {

    public static final String ATTRIBUTE_ORCID_ID = "orcidId";

    public static final String ATTRIBUTE_ORCID_ACCESS_TOKEN = "orcidAccessToken";

    private static final PrincipalFactory PRINCIPAL_FACTORY = PrincipalFactoryUtils.newPrincipalFactory();

    private final String orcidClientName;

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
                .forEach(credential -> captureOrcidToken(credential, builder));
    }

    private void captureOrcidToken(final ClientCredential credential, final AuthenticationBuilder builder) {
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
            final Principal principal = builder.getPrincipal();
            if (principal == null) {
                LOGGER.debug("No principal resolved yet on the authentication builder; skipping ORCID token capture.");
                return;
            }
            final Map<String, List<Object>> attributes = new LinkedHashMap<>(principal.getAttributes());
            attributes.put(ATTRIBUTE_ORCID_ID, List.of(orcidId));
            attributes.put(ATTRIBUTE_ORCID_ACCESS_TOKEN, List.of(accessToken));
            builder.setPrincipal(PRINCIPAL_FACTORY.createPrincipal(principal.getId(), attributes));
            LOGGER.info("Attached ORCID token attributes to principal for ORCID iD [{}]", orcidId);
        } catch (final Exception e) {
            LOGGER.warn("Failed to capture ORCID OAuth token; login proceeds unaffected. Error: {}", e.getMessage());
            LOGGER.debug("Full stack trace of the ORCID token capture failure:", e);
        }
    }
}
