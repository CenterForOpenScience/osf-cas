package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;

import io.cos.cas.oauth.support.OsfCasOAuth20Constants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20IntrospectionEndpointController}.
 *
 * This controller is disabled for OSF CAS and returns 501 NOT IMPLEMENTED for both GET and POST.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 6.0.0
 */
@Slf4j
public class OAuth20IntrospectionEndpointController extends BaseOAuth20Controller {

    public OAuth20IntrospectionEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Build not implemented response entity.
     *
     * @return the response entity
     */
    private static ResponseEntity<OAuth20IntrospectionAccessTokenResponse> buildNotImplementedResponseEntity() {
        val map = new LinkedMultiValueMap<String, String>(1);
        map.add(OAuth20Constants.ERROR, OsfCasOAuth20Constants.NOT_IMPLEMENTED);
        val value = OAuth20Utils.toJson(map);
        val headers = new LinkedMultiValueMap<String, String>();
        return new ResponseEntity(value, headers, HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(
            value = {
                    '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL,
                    '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL + '/'
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handleRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        LOGGER.error("GET {}/{} has not been implemented.", OAuth20Constants.BASE_OAUTH20_URL, OAuth20Constants.INTROSPECTION_URL);
        return buildNotImplementedResponseEntity();
    }

    /**
     * Handle post request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(
            value = {
                    '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL,
                    '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL + '/'
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handlePostRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        LOGGER.error("POST {}/{} has not been implemented.", OAuth20Constants.BASE_OAUTH20_URL, OAuth20Constants.INTROSPECTION_URL);
        return buildNotImplementedResponseEntity();
    }
}
