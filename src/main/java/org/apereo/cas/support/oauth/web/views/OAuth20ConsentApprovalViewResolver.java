package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import io.cos.cas.oauth.support.OsfCasOAuth20Constants;
import io.cos.cas.oauth.support.OsfCasOAuth20ModelContext;
import io.cos.cas.oauth.support.OsfCasOAuth20Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.CommonHelper;

import org.springframework.web.servlet.ModelAndView;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link OAuth20ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20ConsentApprovalViewResolver implements ConsentApprovalViewResolver {

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ModelAndView resolve(final JEEContext context, final OAuthRegisteredService service) {
        return this.resolve(context, null, service);
    }

    @Override
    public ModelAndView resolve(
            final JEEContext context,
            final OsfCasOAuth20ModelContext modelContext,
            final OAuthRegisteredService service
    ) {
        if (modelContext == null) {
            return OsfCasOAuth20Utils.produceOAuth20ErrorView(new OsfCasOAuth20ModelContext(
                    OsfCasOAuth20Constants.INTERNAL_SERVER_ERROR,
                    this.getClass().getName(),
                    OsfCasOAuth20Constants.SERVER_ERROR_MSG,
                    new HashSet<>(),
                    casProperties.getAuthn().getOsfUrl()
            ));
        }

        // Note: Changing the conditions in this method may lead to users being stuck on the approval page. Please get familiar with how
        //       APPROVAL_PROMPT (oldCAS, request param only), BYPASS_APPROVAL_PROMPT (newCAS, both request param and session store),
        //       isConsentApprovalBypassed() and redirectToApproveView() works before making changes.
        var bypassApprovalParameter = context
                .getRequestParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT)
                .map(String::valueOf)
                .orElse(StringUtils.EMPTY);
        LOGGER.trace("bypassApprovalParameter in request params: {}", bypassApprovalParameter);
        if (StringUtils.isBlank(bypassApprovalParameter)) {
            // "osfApprovalPrompt" is effective only if "bypassApprovalParameter" is not present
            var osfApprovalPrompt = context
                    .getRequestParameter(OsfCasOAuth20Constants.APPROVAL_PROMPT)
                    .map(String::valueOf)
                    .orElse(StringUtils.EMPTY);
            LOGGER.trace("osfApprovalPrompt in request params: {}", osfApprovalPrompt);
            // Default is "auto", thus anything else other than "force" is considered as "auto"
            if (osfApprovalPrompt.equalsIgnoreCase(OsfCasOAuth20Constants.APPROVAL_PROMPT_FORCE)) {
                bypassApprovalParameter = Boolean.FALSE.toString();
                LOGGER.trace("bypassApprovalParameter from osfApprovalPrompt: {} <<-- {}", bypassApprovalParameter, osfApprovalPrompt);
            } else {
                bypassApprovalParameter = (String) context
                        .getSessionStore()
                        .get(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT)
                        .map(String::valueOf)
                        .orElse(StringUtils.EMPTY);
                LOGGER.trace("bypassApprovalParameter in session store: {}", bypassApprovalParameter);
            }
        }
        LOGGER.trace("Bypassing approval prompt for service [{}]: [{}]", service, bypassApprovalParameter);
        if (Boolean.TRUE.toString().equalsIgnoreCase(bypassApprovalParameter) || isConsentApprovalBypassed(context, service)) {
            context.getSessionStore().set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
            return new ModelAndView();
        }
        return redirectToApproveView(context, modelContext, service);
    }

    /**
     * Is consent approval bypassed?
     *
     * @param context the context
     * @param service the service
     * @return true/false
     */
    protected boolean isConsentApprovalBypassed(final JEEContext context, final OAuthRegisteredService service) {
        return service.isBypassApprovalPrompt();
    }

    /**
     * Redirect to approve view model and view.
     *
     * @param context       the context
     * @param modelContext  the model context
     * @param service       the service
     * @return the model and view
     */
    protected ModelAndView redirectToApproveView(final JEEContext context, final OsfCasOAuth20ModelContext modelContext, final OAuthRegisteredService service) {
        try {
            val model = modelContext.getModelContextMap();
            model.put("callbackUrl", getCallbackUrl(context));
            model.put("deniedApprovalUrl", getDeniedApprovalUrl(context, service));
            prepareApprovalViewModel(model, service);
            return getApprovalModelAndView(model);
        } catch (URISyntaxException e) {
            modelContext.setErrorCode(OAuth20Constants.INVALID_REQUEST);
            modelContext.setErrorParam(e.getMessage());
            return OsfCasOAuth20Utils.produceOAuth20ErrorView(modelContext);
        }
    }

    /**
     * Build the callback URL for the form submission action when the "Allow" button is clicked.
     *
     * @param context the context
     * @return the callback URL
     * @throws URISyntaxException if the original request URL has bad syntax
     */
    protected String getCallbackUrl(final JEEContext context) throws URISyntaxException {
        val callbackUrl = context.getFullRequestURL();
        val url = new URIBuilder(callbackUrl);
        // APPROVAL_PROMPT can be set to any value. It does not have any effect since BYPASS_APPROVAL_PROMPT is present.
        // However, setting it to EMPTY is preferred so that it is different from its original values "auto" or "force".
        url.setParameter(OsfCasOAuth20Constants.APPROVAL_PROMPT, StringUtils.EMPTY);
        url.setParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        LOGGER.debug("Callback URL for approval submit action: [{}]", callbackUrl);
        return url.toString();

    }

    /**
     * Build the denied approval URL for the form submission action when the "Deny" button is clicked.
     *
     * @param context the context
     * @return the denied approval URL
     */
    protected String getDeniedApprovalUrl(final JEEContext context, OAuthRegisteredService service) {
        var deniedApprovalUrl = context
                .getRequestParameter(OAuth20Constants.REDIRECT_URI)
                .map(String::valueOf)
                .orElse(service.getRedirectUrl());
        deniedApprovalUrl = CommonHelper.addParameter(deniedApprovalUrl, OAuth20Constants.ERROR, OAuth20Constants.ACCESS_DENIED);
        LOGGER.debug("Denied approval URL for denial submit action [{}]", deniedApprovalUrl);
        return deniedApprovalUrl;
    }

    /**
     * Gets approval model and view.
     *
     * @param model the model
     * @return the approval model and view
     */
    protected ModelAndView getApprovalModelAndView(final Map<String, Object> model) {
        return new ModelAndView(getApprovalViewName(), model);
    }

    /**
     * Gets approval view name.
     *
     * @return the approval view name
     */
    protected String getApprovalViewName() {
        return OAuth20Constants.CONFIRM_VIEW;
    }

    /**
     * Prepare approval view model.
     *
     * @param model     the model
     * @param service   the svc
     */
    protected void prepareApprovalViewModel(final Map<String, Object> model, final OAuthRegisteredService service) {
        model.put("service", service);
        model.put("serviceName", service.getName());
        model.put("serviceDescription", service.getDescription());
    }
}
