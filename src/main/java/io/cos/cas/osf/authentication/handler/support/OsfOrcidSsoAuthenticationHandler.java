package io.cos.cas.osf.authentication.handler.support;

import io.cos.cas.osf.authentication.credential.OsfOrcidSsoCredential;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfOrcidSsoAuthenticationHandler}.
 *
 * @author Longze Chen
 * @since 26.2.0
 */
@Getter
@Setter
@Slf4j
public class OsfOrcidSsoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /** Constructor for all required args. */
    public OsfOrcidSsoAuthenticationHandler(
            final String name,
            final ServicesManager servicesManager,
            final PrincipalFactory principalFactory,
            final Integer order
    ) {
        super(name, servicesManager, principalFactory, order);
    }

    /** Authenticate with no-op credential transform. */
    @Override
    protected final AuthenticationHandlerExecutionResult doAuthentication(
            Credential credential
    ) throws GeneralSecurityException {
        OsfOrcidSsoCredential osfOrcidSsoCredential = (OsfOrcidSsoCredential) credential;
        LOGGER.debug("[ORCiD SSO] Attempting authentication internally for transformed credential [{}]", osfOrcidSsoCredential);
        return authenticateOsfOrcidSsoInternal(osfOrcidSsoCredential);
    }

    /** {@link OsfOrcidSsoAuthenticationHandler} only supports {@link OsfOrcidSsoCredential} */
    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OsfOrcidSsoCredential.class.isAssignableFrom(clazz);
    }

    /** {@link OsfOrcidSsoAuthenticationHandler} only supports {@link OsfOrcidSsoCredential} */
    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OsfOrcidSsoCredential;
    }

    /** Create {@link AuthenticationHandlerExecutionResult} from {@link OsfOrcidSsoCredential}. */
    protected final AuthenticationHandlerExecutionResult authenticateOsfOrcidSsoInternal(
            final OsfOrcidSsoCredential credential
    ) throws GeneralSecurityException {

        if (credential == null) {
            LOGGER.error("[ORCiD SSO] Null/Empty ORCiD Credential.");
            throw new GeneralSecurityException("Null/Empty ORCiD Credential.");
        }

        final String credentialId = credential.getId();
        final String orcidId = credential.getOrcidId();
        final String orcidAccessToken = credential.getOrcidAccessToken();
        final String orcidRefreshToken = credential.getOrcidRefreshToken();

        if (StringUtils.isBlank(orcidId)) {
            LOGGER.error("[ORCiD SSO] Null/Empty ORCiD ID.");
            throw new GeneralSecurityException("Null/Empty ORCiD ID.");
        } else if (StringUtils.isBlank(orcidAccessToken)) {
            LOGGER.error("[ORCiD SSO] Null/Empty ORCiD Access Token, orcidId=[{}]", orcidId);
            throw new GeneralSecurityException("Null/Empty ORCiD Access Token.");
        }

        LOGGER.info(
                "[ORCiD SSO] Credential metadata: id=[{}], orcidId=[{}], hasAccessToken=[{}], hasRefreshToken=[{}]",
                credentialId,
                orcidId,
                StringUtils.isNotBlank(orcidAccessToken),
                StringUtils.isNotBlank(orcidRefreshToken)
        );

        final Map<String, List<Object>> attributes = new LinkedHashMap<>();
        attributes.put(OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN, Collections.singletonList(orcidAccessToken));
        attributes.put(OsfOrcidSsoCredential.AUTHENTICATION_ATTRIBUTE_ORCID_REFRESH_TOKEN, Collections.singletonList(orcidRefreshToken));
        final Principal principal = this.principalFactory.createPrincipal(credentialId, attributes);
        final List<MessageDescriptor> warnings = new ArrayList<>();
        return createHandlerResult(credential, principal, warnings);
    }
}
