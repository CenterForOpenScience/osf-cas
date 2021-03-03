package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;

import io.cos.cas.oauth.configuration.model.OsfCasOAuth20PersonalAccessTokenProperties;
import io.cos.cas.osf.configuration.model.OsfUrlProperties;
import io.cos.cas.osf.dao.JpaOsfDao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 3.5.0
 */
@Controller
@RequiredArgsConstructor
@Getter
public abstract class BaseOAuth20Controller {

    private final OAuth20ConfigurationContext oAuthConfigurationContext;

    /**
     * Extract access token from token.
     *
     * @param token the token
     * @return the string
     */
    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(getOAuthConfigurationContext().getAccessTokenJwtBuilder())
            .build()
            .decode(token);
    }

    /**
     * Retrieve the JPA OSF data access model so that all controllers have access to the OSF database.
     *
     * @return a {@link JpaOsfDao} object
     */
    protected JpaOsfDao getJpaOsfDao() {
        return getOAuthConfigurationContext().getJpaOsfDao();
    }

    /**
     * Retrieve the OSF URL properties so that all controllers have access to server environment specific URLs.
     *
     * @return an {@link OsfUrlProperties} object
     */
    protected OsfUrlProperties getOsfUrl() {
        return getOAuthConfigurationContext().getCasProperties().getAuthn().getOsfUrl();
    }

    /**
     * Retrieve settings for OSF CAS personal access tokens.
     *
     * @return an {@link OsfCasOAuth20PersonalAccessTokenProperties} object
     */
    protected OsfCasOAuth20PersonalAccessTokenProperties getPersonalAccessTokenProperties() {
        return getOAuthConfigurationContext().getCasProperties().getAuthn().getOauth().getPersonalAccessToken();
    }
}
