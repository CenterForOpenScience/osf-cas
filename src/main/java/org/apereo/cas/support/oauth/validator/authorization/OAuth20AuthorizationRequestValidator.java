package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.support.oauth.validator.OAuth20RequestValidator;

import io.cos.cas.oauth.support.OsfCasOAuth20ModelContext;

import org.pac4j.core.context.JEEContext;

/**
 * This is {@link OAuth20AuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @author Longze Chen
 * @since 5.2.0
 */
public interface OAuth20AuthorizationRequestValidator extends OAuth20RequestValidator {

    /**
     * Validate request.
     *
     * @param context the context
     * @param modelContext the model context
     * @return {@code true} or {@code false}
     */
    boolean validate(JEEContext context, OsfCasOAuth20ModelContext modelContext);
}
