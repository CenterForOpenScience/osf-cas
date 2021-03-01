package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import io.cos.cas.oauth.support.OsfCasOAuth20ModelContext;

import org.pac4j.core.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.0.0
 */
public interface ConsentApprovalViewResolver {

    /**
     * Resolve model and view.
     *
     * @param context the context
     * @param service the service
     * @return the model and view. Could be an empty view which would indicate consent is not required.
     */
    ModelAndView resolve(JEEContext context, OAuthRegisteredService service);

    /**
     * Resolve model and view.
     *
     * @param context the context
     * @param modelContext the mode context
     * @param service the service
     * @return the model and view. Could be an empty view which would indicate consent is not required.
     */
    ModelAndView resolve(JEEContext context, OsfCasOAuth20ModelContext modelContext, OAuthRegisteredService service);
}
