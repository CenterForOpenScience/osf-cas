package org.apereo.cas.support.oauth.web;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.jetbrains.annotations.NotNull;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link OAuth20HandlerInterceptorAdapter}.
 *
 * This interceptor has been customized for OSF so that certain endpoints no longer required authentication pre-handle.
 *
 * {@code /oauth2/revoke} - OSF allows revoke one token with just the token to revoke. Thus checking client ID for authentication
 *                         pre-handle breaks the endpoint.
 * {@code /oauth2/device} - Disabled endpoints no longer needs extra pre-handle; the same applies to {@code /oauth2/introspect}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth20HandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    /**
     * Access token interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;

    /**
     * Authorization interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor;

    private final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    private final ServicesManager servicesManager;

    private final SessionStore<JEEContext> sessionStore;

    @Override
    public boolean preHandle(
            @NotNull final HttpServletRequest request,
            @NotNull final HttpServletResponse response,
            @NotNull final Object handler
    ) throws Exception {

        if (isRevokeTokenRequest(request) || isDisabledEndpoints(request)) {
            return true;
        }
        if (requestRequiresAuthentication(request)) {
            return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
        }
        if (isAuthorizationRequest(request)) {
            return requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
        }
        return true;
    }

    /**
     * Is authorization request.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isAuthorizationRequest(final HttpServletRequest request) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, OAuth20Constants.AUTHORIZE_URL);
    }

    /**
     * Is a revoke token request?
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isRevokeTokenRequest(final HttpServletRequest request) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, OAuth20Constants.REVOCATION_URL);
    }

    /**
     * Is access token request request.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isAccessTokenRequest(final HttpServletRequest request) {
        val requestPath = request.getRequestURI();
        val pattern = String.format("(%s|%s)", OAuth20Constants.ACCESS_TOKEN_URL, OAuth20Constants.TOKEN_URL);
        return doesUriMatchPattern(requestPath, pattern);
    }

    /**
     * Is device token request boolean.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isDeviceTokenRequest(final HttpServletRequest request) {
        val requestPath = request.getRequestURI();
        val pattern = String.format("(%s)", OAuth20Constants.DEVICE_AUTHZ_URL);
        return doesUriMatchPattern(requestPath, pattern);
    }

    /**
     * Is introspect request boolean.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isIntrospectRequest(final HttpServletRequest request) {
        val requestPath = request.getRequestURI();
        val pattern = String.format("(%s)", OAuth20Constants.INTROSPECTION_URL);
        return doesUriMatchPattern(requestPath, pattern);
    }

    /**
     * Is disabled endpoints request.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean isDisabledEndpoints(final HttpServletRequest request) {
        return isDeviceTokenRequest(request) || isIntrospectRequest(request);
    }

    /**
     * Request requires authentication.
     *
     * @param request  the request
     * @return true/false
     */
    protected boolean requestRequiresAuthentication(final HttpServletRequest request) {

        val accessTokenRequest = isAccessTokenRequest(request);
        if (!accessTokenRequest) {
            val extractor = extractAccessTokenGrantRequest(request);
            if (extractor.isPresent()) {
                val ext = extractor.get();
                return ext.requestMustBeAuthenticated();
            }
        } else {
            val extractor = extractAccessTokenGrantRequest(request);
            if (extractor.isPresent()) {
                val ext = extractor.get();
                return ext.getResponseType() != OAuth20ResponseTypes.DEVICE_CODE;
            }
        }
        return false;
    }

    private Optional<AccessTokenGrantRequestExtractor> extractAccessTokenGrantRequest(final HttpServletRequest request) {
        return this.accessTokenGrantRequestExtractors
            .stream()
            .filter(ext -> ext.supports(request))
            .findFirst();
    }

    /**
     * Does uri match pattern.
     *
     * @param requestPath the request path
     * @param patternUrl  the pattern
     * @return true/false
     */
    protected boolean doesUriMatchPattern(final String requestPath, final String patternUrl) {
        val pattern = Pattern.compile('/' + patternUrl + "(/)*$");
        return pattern.matcher(requestPath).find();
    }
}
