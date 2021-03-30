package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.context.JEEContext;

import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20OneTokenRevocationRequestValidator}.
 *
 * @author Longze Chen
 * @since 6.2.8
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class OAuth20OneTokenRevocationRequestValidator implements OAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final JEEContext context) {
        return true;
    }

    @Override
    public boolean supports(final JEEContext context) {
        val token = context.getRequestParameter(OAuth20Constants.TOKEN).map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        val clientSecret = OAuth20Utils.getClientIdAndClientSecret(context).getRight();
        return StringUtils.isNotBlank(token) && StringUtils.isBlank(clientId) && StringUtils.isBlank(clientSecret);
    }
}
