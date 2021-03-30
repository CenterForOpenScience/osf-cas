package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpRequestUtils;

import io.cos.cas.oauth.support.OsfCasOAuth20Constants;
import io.cos.cas.oauth.support.OsfCasOAuth20ModelContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.context.JEEContext;

import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator implements OAuth20AuthorizationRequestValidator {

    private static final List<String> REQUIRED_PARAMS = new ArrayList<>(Arrays.asList(
            OAuth20Constants.CLIENT_ID,
            OAuth20Constants.REDIRECT_URI,
            OAuth20Constants.SCOPE,
            OAuth20Constants.RESPONSE_TYPE
    ));

    /**
     * Service manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    /**
     * Service access enforcer.
     */
    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final JEEContext context, final OsfCasOAuth20ModelContext modelContext) {

        // With OSF CAS OAuth, model context must be provided to store validation error information to be sent to the view.
        if (modelContext == null) {
            LOGGER.error("Validation without model context is no longer supported for class [{}]", getClass().getName());
            return false;
        }

        // Required params must exists and must not be empty. `HttpRequestUtils.doesParameterExist()` check is case-sensitive.
        val request = context.getNativeRequest();
        for (val requiredParam: REQUIRED_PARAMS) {
            if (!HttpRequestUtils.doesParameterExist(request, requiredParam)) {
                LOGGER.error("Missing required parameters [{}]", requiredParam);
                modelContext.setErrorCode(OsfCasOAuth20Constants.MISSING_PARAM);
                modelContext.setErrorParam(requiredParam);
                return false;
            }
        }

        // Not-allowed params must not present.
        val authnRequest = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isNotBlank(authnRequest)) {
            LOGGER.error("Self-contained authentication requests as JWTs are not accepted");
            modelContext.setErrorCode(OsfCasOAuth20Constants.PARAM_NOT_ALLOWED);
            modelContext.setErrorParam(OAuth20Constants.REQUEST);
            return false;
        }

        // This check is redundant since the `supports()` already guarantees the response type is one of `OAuth20ResponseTypes`.
        val responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        if (!OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.error(
                    "Response type [{}] is not found in the list of supported values [{}].",
                    responseType,
                    OAuth20ResponseTypes.values()
            );
            modelContext.setErrorCode(OsfCasOAuth20Constants.INVALID_RESP_TYPE);
            modelContext.setErrorParam(responseType);
            return false;
        }

        // Client ID must have a matching registered service.
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = getRegisteredServiceByClientId(clientId);
        if (registeredService == null) {
            LOGGER.error("Registered service with client ID [{}] is not found.", clientId);
            modelContext.setErrorCode(OAuth20Constants.INVALID_CLIENT);
            modelContext.setErrorParam(clientId);
            return false;
        }

        // Check service access against the service access strategy enforcer.
        val service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
        val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        if (accessResult.isExecutionFailure()) {
            LOGGER.error("Registered service [{}] is not authorized for access.", registeredService);
            modelContext.setErrorCode(OsfCasOAuth20Constants.SERVICE_NOT_ALLOWED);
            modelContext.setErrorParam(registeredService.getServiceId());
            return false;
        }

        // Redirect / callback URI must match the registered service.
        val redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        if (!OAuth20Utils.checkCallbackValid(registeredService, redirectUri)) {
            LOGGER.error("Callback URL [{}] is not authorized for registered service [{}].", redirectUri, registeredService);
            modelContext.setErrorCode(OsfCasOAuth20Constants.INVALID_REDIRECT_URI);
            modelContext.setErrorParam(redirectUri);
            return false;
        }

        // The registered service must have defined the response code as an authorized one.
        if (!OAuth20Utils.isAuthorizedResponseTypeForService(context, registeredService)) {
            LOGGER.error("[{}] is not an authorized response type for registered service [{}].", responseType, registeredService);
            modelContext.setErrorCode(OsfCasOAuth20Constants.RESP_TYPE_NOT_ALLOWED);
            modelContext.setErrorParam(registeredService.getServiceId());
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final JEEContext context) {
        return validate(context, null);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return OAuth20Utils.isResponseType(responseType.map(String::valueOf).orElse(StringUtils.EMPTY), getResponseType());
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.CODE;
    }
}
