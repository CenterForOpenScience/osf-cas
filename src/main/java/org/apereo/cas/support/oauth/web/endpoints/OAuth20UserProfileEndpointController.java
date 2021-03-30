package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import io.cos.cas.oauth.model.OsfCasOAuth20CodeType;
import io.cos.cas.oauth.ticket.accesstoken.OsfCasOAuth20PersonalAccessToken;
import io.cos.cas.oauth.ticket.support.OsfCasOAuth20PersonalAccessTokenExpirationPolicy;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.JEEContext;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This controller returns a profile for the authenticated user
 * (identifier + attributes), found with the access token.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 3.5.0
 */
@Slf4j
public class OAuth20UserProfileEndpointController extends BaseOAuth20Controller {
    private final ResponseEntity<String> expiredAccessTokenResponseEntity;

    public OAuth20UserProfileEndpointController(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
        this.expiredAccessTokenResponseEntity = buildUnauthorizedResponseEntity(OAuth20Constants.EXPIRED_ACCESS_TOKEN);
    }

    /**
     * Build unauthorized response entity.
     *
     * @param code the code
     * @return the response entity
     */
    protected static ResponseEntity buildUnauthorizedResponseEntity(final String code) {
        val map = new LinkedMultiValueMap<String, String>(1);
        map.add(OAuth20Constants.ERROR, code);
        val value = OAuth20Utils.toJson(map);
        return new ResponseEntity<>(value, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle request internal response entity.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping(
            path = {
                    OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.PROFILE_URL,
                    OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.PROFILE_URL + '/'
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleGetRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val accessTokenId = getAccessTokenFromRequest(request);
        if (StringUtils.isBlank(accessTokenId)) {
            LOGGER.error("Missing [{}] from the request", OAuth20Constants.ACCESS_TOKEN);
            return buildUnauthorizedResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN);
        }

        OAuth20AccessToken accessTokenTicket;
        if (accessTokenId.startsWith(OAuth20AccessToken.PREFIX + UniqueTicketIdGenerator.SEPARATOR)) {
            // CAS vanilla access token must starts with "AT-".
           accessTokenTicket = getOAuthConfigurationContext()
                    .getTicketRegistry()
                    .getTicket(accessTokenId, OAuth20AccessToken.class);
        } else {
            // Due to the fact that OSF personal access token is just a random string, it is possible that a CAS
            // personal access token exists when a vanilla one can't be found or has expired. The CAS counterpart
            // of the OSF personal access token has an extra prefix "OSFPAT-".
            val personalAccessTokenId = OsfCasOAuth20PersonalAccessToken.PREFIX + UniqueTicketIdGenerator.SEPARATOR + accessTokenId;
            accessTokenTicket = getOAuthConfigurationContext()
                    .getTicketRegistry()
                    .getTicket(personalAccessTokenId, OAuth20AccessToken.class);
        }
        if (accessTokenTicket == null || accessTokenTicket.isExpired()) {
            // If neither is found nor active, remove unexpired ticket first.
            LOGGER.debug("Access token [{}] cannot be found in the ticket registry or has expired.", accessTokenId);
            if (accessTokenTicket != null) {
                LOGGER.debug("Delete expired access token  [{}].", accessTokenId);
                getOAuthConfigurationContext().getTicketRegistry().deleteTicket(accessTokenTicket);
            }
            // Then try to create an CAS access token if the token provided is an valid and active OSF personal access token.
            LOGGER.debug("Check personal access token [{}] in the OSF database.", accessTokenId);
            accessTokenTicket = attemptToGenerateAccessTokenFromOsfPat(accessTokenId);
            // Raise authorization error if all attempts have been failed.
            if (accessTokenTicket == null) {
                LOGGER.error("Access token [{}] can neither be found in the ticket registry nor the OSF database.", accessTokenId);
                return expiredAccessTokenResponseEntity;
            }
        }

        updateAccessTokenUsage(accessTokenTicket);
        val map = getOAuthConfigurationContext().getUserProfileDataCreator().createFrom(accessTokenTicket, context);
        return getOAuthConfigurationContext().getUserProfileViewRenderer().render(map, accessTokenTicket, response);
    }

    /**
     * Update the access token in the registry.
     *
     * @param accessTokenTicket the access token
     */
    protected void updateAccessTokenUsage(final OAuth20AccessToken accessTokenTicket) {
        val accessTokenState = TicketState.class.cast(accessTokenTicket);
        accessTokenState.update();
        if (accessTokenTicket.isExpired()) {
            getOAuthConfigurationContext().getTicketRegistry().deleteTicket(accessTokenTicket.getId());
        } else {
            getOAuthConfigurationContext().getTicketRegistry().updateTicket(accessTokenTicket);
        }
    }

    /**
     * Gets access token from request.
     *
     * @param request the request
     * @return the access token from request
     */
    protected String getAccessTokenFromRequest(final HttpServletRequest request) {
        var accessToken = request.getParameter(OAuth20Constants.ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            val authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotBlank(authHeader) && authHeader.toLowerCase()
                .startsWith(OAuth20Constants.TOKEN_TYPE_BEARER.toLowerCase() + ' ')) {
                accessToken = authHeader.substring(OAuth20Constants.TOKEN_TYPE_BEARER.length() + 1);
            }
        }
        LOGGER.debug("[{}]: [{}]", OAuth20Constants.ACCESS_TOKEN, accessToken);
        return extractAccessTokenFrom(accessToken);
    }

    /**
     * Check if an OSF personal access token {@link io.cos.cas.osf.model.OsfOAuth20Pat} exists in the OSF database.
     *
     * If so, create an access token {@link OAuth20DefaultAccessToken} of type {@link OsfCasOAuth20CodeType#PERSONAL} with
     * this expiration policy {@link OsfCasOAuth20PersonalAccessTokenExpirationPolicy}; otherwise, return {@code null}
     * to indicate a failed attempt.
     *
     * @param tokenId the token ID
     * @return an {@link OAuth20DefaultAccessToken} object or {@code null}
     */
    protected OAuth20AccessToken attemptToGenerateAccessTokenFromOsfPat(final String tokenId) {

        // Attempt to find the OSF personal access token by the given token ID from the OSF database
        val osfPersonalAccessToken = getJpaOsfDao().findOnePatByTokenId(tokenId);
        if (osfPersonalAccessToken == null) {
            LOGGER.warn("Cannot find OSF OAuth 2.0 PAT with id=[{}] in the OSF database", tokenId);
            return null;
        }
        val personalAccessTokenId = OsfCasOAuth20PersonalAccessToken.PREFIX + UniqueTicketIdGenerator.SEPARATOR + tokenId;
        // Build the required scope set
        val osfOAuth20tokenScopes = getJpaOsfDao().findAllPatScopesByTokenPk(osfPersonalAccessToken.getId());
        val scopeSet = new HashSet<String>();
        osfOAuth20tokenScopes.forEach(item -> {
            val scope= getJpaOsfDao().findOneScopeByScopePk(item.getScopePk());
            if (scope != null) {
                scopeSet.add(scope.getName());
            }
        });
        // Check and retrieve the owner's GUID of the OSF PAT and build the required principal
        val osfGuid = getJpaOsfDao().findGuidByUser(osfPersonalAccessToken.getOwner());
        if (osfGuid == null) {
            LOGGER.warn("Cannot find the user GUID for PAT with id=[{}] and ownerId=[{}]", tokenId, osfPersonalAccessToken.getOwner().getId());
            return null;
        }
        val principal = new DefaultPrincipalFactory().createPrincipal(osfGuid.getGuid());
        // Build the required authentication with the current time, the principal, and other required args (with empty value)
        val authenticationDate = ZonedDateTime.now(ZoneOffset.UTC);
        val authentication = new DefaultAuthentication(
                authenticationDate,
                principal,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>()
        );
        // Build the required service with OSF home set as the `originalUrl`
        val osfUrlProperties = getOsfUrl();
        val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(osfUrlProperties.getHome());
        // Configure the expiration policy using `OsfCasOAuth20PersonalAccessTokenExpirationPolicy`
        val osfPatProperties = getPersonalAccessTokenProperties();
        val expirationPolicy = new OsfCasOAuth20PersonalAccessTokenExpirationPolicy(
                Beans.newDuration(osfPatProperties.getMaxTimeToLiveInSeconds()).getSeconds(),
                Beans.newDuration(osfPatProperties.getTimeToKillInSeconds()).getSeconds()
        );
        // Create the CAS personal access token and added the ticket registry
        val accessToken = new OAuth20DefaultAccessToken(
                personalAccessTokenId,
                service,
                authentication,
                expirationPolicy,
                null,
                scopeSet,
                null,
                new HashMap<>()
        );
        accessToken.setOsfType(OsfCasOAuth20CodeType.PERSONAL.getValue());
        getOAuthConfigurationContext().getTicketRegistry().addTicket(accessToken);

        return accessToken;
    }
}
