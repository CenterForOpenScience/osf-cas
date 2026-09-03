package org.pac4j.oauth.profile.creator;

import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.credentials.OAuth20Credentials;
import org.pac4j.oauth.profile.OAuth20Profile;

/**
 * OAuth 2.0 profile creator.
 *
 * <p>OSF CAS Customizations: modified {@link #addAccessTokenToProfile(OAuth20Profile, OAuth2AccessToken)} to include
 * refresh token in profile attributes.</p>
 *
 * @author Jerome Leleu
 * @author Longze Chen
 * @since 2.0.0
 * @version 4.1.0
 */
public class OAuth20ProfileCreator<U extends OAuth20Profile>
        extends OAuthProfileCreator<OAuth20Credentials, U, OAuth20Configuration, OAuth2AccessToken, OAuth20Service> {

    private static final String REFRESH_TOKEN = "refresh_token";

    public OAuth20ProfileCreator(final OAuth20Configuration configuration, final IndirectClient client) {
        super(configuration, client);
    }

    @Override
    protected OAuth2AccessToken getAccessToken(final OAuth20Credentials credentials) {
        return credentials.getAccessToken();
    }

    @Override
    protected void addAccessTokenToProfile(final U profile, final OAuth2AccessToken accessToken) {
        if (profile != null) {
            // Add access token
            final String access_token = accessToken.getAccessToken();
            logger.debug("[OAuth20 SSO] Add access token to profile: isAccessTokenNotBlank=[{}]", StringUtils.isNotBlank(access_token));
            profile.setAccessToken(access_token);

            // Add refresh token manually instead of war-overlaying and customizing org.pac4j.oauth.profile.OAuth20Profile
            final String refreshToken = accessToken.getRefreshToken();
            if (StringUtils.isNoneBlank(refreshToken)) {
                logger.debug("[OAuth20 SSO] Refresh token found, adding it to profile");
                profile.addAttribute(REFRESH_TOKEN, refreshToken);
            } else {
                logger.debug("[OAuth20 SSO] Refresh token not found, adding empty value to profile");
                profile.addAttribute(REFRESH_TOKEN, StringUtils.EMPTY);
            }
        }
    }

    @Override
    protected void signRequest(final OAuth20Service service, final OAuth2AccessToken accessToken,
                               final OAuthRequest request) {
        service.signRequest(accessToken, request);
        if (this.configuration.isTokenAsHeader()) {
            request.addHeader(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BEARER_HEADER_PREFIX + accessToken.getAccessToken());
        }
        if (Verb.POST.equals(request.getVerb())) {
            request.addParameter(OAuthConfiguration.OAUTH_TOKEN, accessToken.getAccessToken());
        }
    }
}
