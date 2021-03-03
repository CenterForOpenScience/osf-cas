package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import io.cos.cas.oauth.support.OsfCasOAuth20Utils;
import io.cos.cas.oauth.support.OsfCasOAuth20Constants;
import io.cos.cas.oauth.ticket.accesstoken.OsfCasOAuth20PersonalAccessToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.context.JEEContext;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20RevocationEndpointController}.
 *
 * @author Julien Huon
 * @author Longze Chen
 * @since 6.2.0
 */
@Slf4j
public class OAuth20RevocationEndpointController extends BaseOAuth20Controller {

    public OAuth20RevocationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request for revocation.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(
            path = {
                    OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.REVOCATION_URL,
                    OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.REVOCATION_URL + '/'
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ModelAndView handleRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());

        if (!verifyRevocationRequest(context)) {
            LOGGER.error("Revocation request verification failed. Request is missing required parameters");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val tokenId = context.getRequestParameter(OAuth20Constants.TOKEN).map(String::valueOf).orElse(StringUtils.EMPTY);
        String personalAccessTokenId = null;
        String clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        val clientSecret = OAuth20Utils.getClientIdAndClientSecret(context).getRight();
        OAuthRegisteredService registeredService;

        if (StringUtils.isNotBlank(tokenId) && StringUtils.isBlank(clientId) && StringUtils.isBlank(clientSecret)) {
            // Remove one token
            OAuth20Token token = null;
            if (tokenId.startsWith(OAuth20AccessToken.PREFIX) || tokenId.startsWith(OAuth20RefreshToken.PREFIX)) {
                token = getOAuthConfigurationContext().getTicketRegistry().getTicket(tokenId, OAuth20Token.class);
            } else {
                personalAccessTokenId = OsfCasOAuth20PersonalAccessToken.PREFIX + UniqueTicketIdGenerator.SEPARATOR + tokenId;
                token = getOAuthConfigurationContext().getTicketRegistry().getTicket(personalAccessTokenId, OAuth20Token.class);
            }
            if (token == null) {
                LOGGER.error("expired or invalid token [{}]", tokenId);
                return OAuth20Utils.writeError(response, OAuth20Constants.EXPIRED_ACCESS_TOKEN);
            }
            if (personalAccessTokenId == null) {
                clientId = token.getClientId();
                registeredService = getRegisteredServiceByClientId(clientId);
                LOGGER.debug("token verified for service [{}]", clientId);
                if (accessResult(registeredService).isExecutionFailure()) {
                    return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
                }
            }
            return generateOneTokenRevocationResponse(token, response);
        } else if (StringUtils.isBlank(tokenId) && StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(clientSecret)) {
            // Remove all tokens that belong to a client / service
            registeredService = getRegisteredServiceByClientId(clientId);
            if (!OAuth20Utils.checkClientSecret(registeredService, clientSecret, getOAuthConfigurationContext().getRegisteredServiceCipherExecutor())) {
                LOGGER.error("client secret check failed for service [{}]", clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.ACCESS_DENIED);
            }
            if (accessResult(registeredService).isExecutionFailure()) {
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
            }
            return generateAllClientTokensRevocationResponse(registeredService, response);
        } else {
            LOGGER.error("Revocation request verification failed. Request has unexpected parameters.");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }
    }

    /**
     * Registered service access enforcement.
     *
     * @param registeredService the registered service
     * @return an auditable execution result
     */
    protected AuditableExecutionResult accessResult(final OAuthRegisteredService registeredService) {
        val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());
        val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
        return getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
    }

    /**
     * Attempt to revoke one token and generate the revocation response.
     *
     * @param registryToken the token to revoke
     * @param response the response
     * @return the model and view
     */
    protected ModelAndView generateOneTokenRevocationResponse(final OAuth20Token registryToken, final HttpServletResponse response) {
        if (registryToken == null) {
            LOGGER.error("The token is null");
        } else if (isRefreshToken(registryToken)) {
                revokeToken((OAuth20RefreshToken) registryToken);
        } else if (isAccessToken(registryToken)) {
            revokeToken(registryToken.getId());
        } else {
            LOGGER.error("Provided token [{}] is either not a refresh token or not an access token", registryToken.getId());
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }
        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.NO_CONTENT);
        return mv;
    }

    /**
     * Attempt to revoke all tokens associated with a given client / service and generate the revocation response.
     *
     * @param registeredService the client who requests the revocation
     * @param response the response
     * @return the model and view
     */
    protected ModelAndView generateAllClientTokensRevocationResponse(final OAuthRegisteredService registeredService, final HttpServletResponse response) {

        val ticketRegistry = getOAuthConfigurationContext().getTicketRegistry();
        val clientId = registeredService.getClientId();
        try {
            // Retrieve a map with two entries: access token list and refresh token list.
            val tokens = OsfCasOAuth20Utils.getOAuth20ClientAccessAndRefreshTokens(ticketRegistry, clientId);
            // Removing refresh tokens (and access tokens generated by them)
            tokens.get(OAuth20RefreshToken.PREFIX).forEach(token -> revokeToken((OAuth20RefreshToken) token));
            // Remove access tokens (that are not generated by refresh tokens)
            tokens.get(OAuth20AccessToken.PREFIX).forEach(token -> revokeToken(token.getId()));
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve and revoke tokens for client [{}]", clientId);
            LOGGER.error("Error message: [{}]", e.getMessage());
            return OsfCasOAuth20Utils.writeError(response, OsfCasOAuth20Constants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.NO_CONTENT);
        return mv;
    }

    /**
     * Revoke the provided Refresh Token and it's related Access Tokens.
     *
     * @param token the token
     */
    private void revokeToken(final OAuth20RefreshToken token) {
        revokeToken(token.getId());
        token.getAccessTokens().forEach(this::revokeToken);
    }

    /**
     * Revoke the provided OAuth Token.
     *
     * @param token the token
     */
    private void revokeToken(final String token) {
        LOGGER.debug("Revoking token [{}]", token);
        getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token);
    }

    /**
     * Is the OAuth token a Refresh Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    private boolean isRefreshToken(final OAuth20Token token) {
        return token instanceof OAuth20RefreshToken;
    }

    /**
     * Is the OAuth token an Access Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    private boolean isAccessToken(final OAuth20Token token) {
        return token instanceof OAuth20AccessToken;
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    private OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Verify the revocation request.
     *
     * @param context the context
     * @return whether the authorize request is valid
     */
    private boolean verifyRevocationRequest(final JEEContext context) {
        val validator = getOAuthConfigurationContext().getAccessTokenGrantRequestValidators()
            .stream()
            .filter(b -> b.supports(context))
            .findFirst()
            .orElse(null);
        if (validator == null) {
            LOGGER.warn("Ignoring malformed request [{}] as no OAuth20 validator could declare support for its syntax", context.getFullRequestURL());
            return false;
        }
        return validator.validate(context);
    }
}
