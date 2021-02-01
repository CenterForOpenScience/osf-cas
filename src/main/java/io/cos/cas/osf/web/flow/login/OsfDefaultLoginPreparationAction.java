package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.configuration.model.OsfUrlProperties;
import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;
import io.cos.cas.osf.web.support.OsfCasLoginContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OsfDefaultLoginPreparationAction}.
 *
 * Extends {@link OsfAbstractLoginPreparationAction} to 1) check and verify parameters and variables in both the flow
 * scope and the request scope; 2) create a new / update the current login context which includes information about
 * pass, current and potential future states; and finally 3) return respective transition events to the login web flow.
 *
 * @author Longze Chen
 * @since 20.1.0
 */
@Slf4j
public class OsfDefaultLoginPreparationAction extends OsfAbstractLoginPreparationAction {

    public OsfDefaultLoginPreparationAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
    }

    @Override
    protected Event doExecute(RequestContext context) {

        final boolean institutionLogin = isInstitutionLogin(context);
        final String institutionId = getInstitutionIdFromRequestContext(context);
        final boolean orcidRedirect = isOrcidLoginAutoRedirect(context);
        final String orcidLoginUrl = getOrcidLoginUrlFromFlowScope(context);

        final String encodedServiceUrl = getEncodedServiceUrlFromRequestContext(context);
        final boolean defaultService = isFromFlowlessErrorPage(context);
        final OsfUrlProperties osfUrl = Optional.of(context).map(
                requestContext -> (OsfUrlProperties) requestContext.getFlowScope().get(OsfCasWebflowConstants.FLOW_PARAMETER_OSF_URL)
        ).orElse(null);
        if (osfUrl == null) {
            LOGGER.error("The login web flow has not been initialized correctly.");
            return error();
        }
        final String defaultServiceUrl = osfUrl.constructDefaultServiceUrl();

        OsfCasLoginContext loginContext;
        loginContext = Optional.of(context).map(requestContext
                -> (OsfCasLoginContext) requestContext.getFlowScope().get(PARAMETER_LOGIN_CONTEXT)).orElse(null);
        if (loginContext == null) {
            loginContext = new OsfCasLoginContext(
                    encodedServiceUrl,
                    institutionLogin,
                    institutionId,
                    orcidRedirect,
                    orcidLoginUrl,
                    defaultService,
                    defaultServiceUrl
            );
        } else {
            loginContext.setDefaultServiceUrl(encodedServiceUrl);
            loginContext.setInstitutionLogin(institutionLogin);
            loginContext.setInstitutionId(institutionId);
            loginContext.setOrcidLoginUrl(orcidLoginUrl);
            loginContext.setOrcidRedirect(false);
            loginContext.setDefaultService(defaultService);
            loginContext.setDefaultServiceUrl(defaultServiceUrl);
        }
        context.getFlowScope().put(PARAMETER_LOGIN_CONTEXT, loginContext);

        if (loginContext.isDefaultService()) {
            if (StringUtils.isNotBlank(defaultServiceUrl)) {
                return autoRedirectToDefaultServiceLogin();
            }
            LOGGER.error("Default service login auto-redirect failed due to URL configurations not found in context.");
            return error();
        } else if (loginContext.isInstitutionLogin()) {
            return switchToInstitutionLogin();
        } else if (loginContext.isOrcidRedirect()) {
            if (StringUtils.isNotBlank(orcidLoginUrl)) {
                return autoRedirectToOrcidLogin();
            }
            LOGGER.error("ORCiD login auto-redirect failed due to delegation configurations not found in context.");
            return error();
        } else {
            return continueToUsernamePasswordLogin();
        }
    }

    private boolean isInstitutionLogin(final RequestContext context) {
        final String campaign = context.getRequestParameters().get(PARAMETER_CAMPAIGN);
        return StringUtils.isNotBlank(campaign) && PARAMETER_CAMPAIGN_VALUE.equals(campaign.toLowerCase());
    }

    private String getInstitutionIdFromRequestContext(final RequestContext context) {
        final String institutionId = context.getRequestParameters().get(PARAMETER_INSTITUTION_ID);
        return StringUtils.isNotBlank(institutionId) ? institutionId : null;
    }

    private boolean isOrcidLoginAutoRedirect(final RequestContext context) {
        final String orcidRedirect = context.getRequestParameters().get(PARAMETER_ORCID_REDIRECT);
        return StringUtils.isNotBlank(orcidRedirect)
                && PARAMETER_ORCID_REDIRECT_VALUE.equals(orcidRedirect.toLowerCase());
    }

    private String getOrcidLoginUrlFromFlowScope(final RequestContext context) {
        final Set<? extends Serializable> clients = WebUtils.getDelegatedAuthenticationProviderConfigurations(context);
        for (final Serializable client: clients) {
            if (client instanceof DelegatedClientIdentityProviderConfiguration) {
                final String clientType = ((DelegatedClientIdentityProviderConfiguration) client).getType();
                if (PARAMETER_ORCID_CLIENT_TYPE.equals(clientType)) {
                    return ((DelegatedClientIdentityProviderConfiguration) client).getRedirectUrl();
                }
            }
        }
        return null;
    }

    private String getEncodedServiceUrlFromRequestContext(final RequestContext context) throws AssertionError {
        final String serviceUrl = context.getRequestParameters().get(PARAMETER_SERVICE);
        if (StringUtils.isBlank(serviceUrl)) {
            return null;
        }
        return URLEncoder.encode(serviceUrl, StandardCharsets.UTF_8);
    }

    private boolean isFromFlowlessErrorPage(final RequestContext context) {
        final String errorCode = context.getRequestParameters().get(PARAMETER_REDIRECT_SOURCE);
        return !StringUtils.isBlank(errorCode) && EXPECTED_REDIRECT_CODES.contains(errorCode);
    }

    private Event continueToUsernamePasswordLogin() {
        return new Event(this, OsfCasWebflowConstants.TRANSITION_ID_USERNAME_PASSWORD_LOGIN);
    }

    private Event switchToInstitutionLogin() {
        return new Event(this, OsfCasWebflowConstants.TRANSITION_ID_INSTITUTION_LOGIN);
    }

    private Event autoRedirectToOrcidLogin() {
        return new Event(this, OsfCasWebflowConstants.TRANSITION_ID_ORCID_LOGIN_AUTO_REDIRECT);
    }

    private Event autoRedirectToDefaultServiceLogin() {
        return new Event(this, OsfCasWebflowConstants.TRANSITION_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT);
    }
}
