package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;

import io.cos.cas.oauth.support.OsfCasOAuth20Constants;
import io.cos.cas.oauth.support.OsfCasOAuth20ModelContext;
import io.cos.cas.oauth.support.OsfCasOAuth20Utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointController}.
 *
 * This controller is disabled for OSF CAS and returns authorization error page for GET or 501 NOT IMPLEMENTED for POST.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 6.0.0
 */
@Slf4j
public class OAuth20DeviceUserCodeApprovalEndpointController extends BaseOAuth20Controller {

    public OAuth20DeviceUserCodeApprovalEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping(path = {
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL,
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL + '/'
    })
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val modelContext = new OsfCasOAuth20ModelContext();
        modelContext.setErrorCode(OsfCasOAuth20Constants.NOT_IMPLEMENTED);
        modelContext.setErrorParam(OAuth20Constants.BASE_OAUTH20_URL + "/" + OAuth20Constants.DEVICE_AUTHZ_URL);
        modelContext.setErrorMsg(String.format("GET %s has not been implemented.", modelContext.getErrorParam()));
        modelContext.setOsfUrl(getOsfUrl());
        LOGGER.error(modelContext.getErrorMsg());
        return OsfCasOAuth20Utils.produceOAuth20ErrorView(modelContext);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @PostMapping(path = {
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL,
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.DEVICE_AUTHZ_URL + '/'
    })
    public ModelAndView handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.error("POST {}/{} has not been implemented.", OAuth20Constants.BASE_OAUTH20_URL, OAuth20Constants.DEVICE_AUTHZ_URL);
        return OsfCasOAuth20Utils.writeError(response, OsfCasOAuth20Constants.NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED);
    }
}
