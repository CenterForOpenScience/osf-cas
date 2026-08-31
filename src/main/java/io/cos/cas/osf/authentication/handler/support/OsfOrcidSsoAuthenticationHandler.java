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

@Getter
@Setter
@Slf4j
public class OsfOrcidSsoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public OsfOrcidSsoAuthenticationHandler(
            final String name,
            final ServicesManager servicesManager,
            final PrincipalFactory principalFactory,
            final Integer order
    ) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected final AuthenticationHandlerExecutionResult doAuthentication(
            Credential credential
    ) throws GeneralSecurityException {
        OsfOrcidSsoCredential osfOrcidSsoCredential = (OsfOrcidSsoCredential) credential;
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]", osfOrcidSsoCredential);
        return authenticateOsfOrcidSsoInternal(osfOrcidSsoCredential);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OsfOrcidSsoCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OsfOrcidSsoCredential;
    }

    protected final AuthenticationHandlerExecutionResult authenticateOsfOrcidSsoInternal(
            final OsfOrcidSsoCredential credential
    ) throws GeneralSecurityException {

        final String credentialId = credential.getId();
        final String orcidId = credential.getOrcidId();
        final String orcidAccessToken = credential.getOrcidAccessToken();
        final String orcidRefreshToken = credential.getOrcidRefreshToken();

        LOGGER.debug(">>>> credential = {}", credential);
        LOGGER.debug(">>>> ---- credentialId = {}", credentialId);
        LOGGER.debug(">>>> ---- orcidId = {}", orcidId);
        LOGGER.debug(">>>> ---- orcidAccessToken = {}", orcidAccessToken);
        LOGGER.debug(">>>> ---- orcidRefreshToken = {}", orcidRefreshToken);

        LOGGER.debug(
                "Credential metadata: id=[{}], orcidId=[{}], orcidAccessToken=[{}], orcidRefreshToken=[{}]",
                credentialId,
                orcidId,
                StringUtils.isNoneBlank(orcidAccessToken),
                StringUtils.isNoneBlank(orcidRefreshToken)
        );

        final Map<String, List<Object>> attributes = new LinkedHashMap<>();
        attributes.put("orcidAccessToken", Collections.singletonList(orcidAccessToken));
        attributes.put("orcidRefreshToken", Collections.singletonList(orcidRefreshToken));
        final Principal principal = this.principalFactory.createPrincipal(credentialId, attributes);
        final List<MessageDescriptor> warnings = new ArrayList<>();
        return createHandlerResult(credential, principal, warnings);
    }
}
