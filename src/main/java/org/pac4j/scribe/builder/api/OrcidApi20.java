package org.pac4j.scribe.builder.api;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import java.util.Map;
import org.pac4j.scribe.extractors.OrcidJsonExtractor;

/**
 * This class represents the OAuth API implementation for ORCiD using OAuth protocol version 2.
 *
 * @author Jens Tinglev
 * @author Longze Chen
 * @since 1.6.0
 * @version 4.0.3
 */
public class OrcidApi20 extends DefaultApi20 {

    private static final String AUTH_URL = "http://www.orcid.org/oauth/authorize";
    private static final String TOKEN_URL = "https://orcid.org/oauth/token";

    @Override
    public String getAccessTokenEndpoint() {
        return TOKEN_URL;
    }

    @Override
    public String getAuthorizationUrl(String responseType, String apiKey, String callback, String scope, String state,
                                      Map<String, String> additionalParams) {
        // The following code breaks the token request with "Redirect URI mismatch." from ORCID API. As stated in
        // https://members.orcid.org/api/resources/error-codes, "redirect_uri in the request for the authorization
        // code must match the redirect_uri used when exchanging the authorization code for an access token", adding
        // "#show_login" leads to different redirect_uri between authorization and token exchange.
        // if (callback != null && !callback.endsWith("#show_login")) {
        //    callback = callback + "#show_login";
        // }
        return super.getAuthorizationUrl(responseType, apiKey, callback, scope, state, additionalParams);
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return AUTH_URL;
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OrcidJsonExtractor.instance();
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
}
