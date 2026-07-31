package io.cos.cas.osf.authentication.support;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

/**
 * This is {@link OrcidTokenRevocationClient}.
 *
 * Calls ORCID's own OAuth revocation endpoint ({@code POST https://orcid.org/oauth/revoke}). Used both by
 * {@code OrcidTokenCaptureAuthenticationPostProcessor} (best-effort revoke-then-replace on reconnect) and by
 * {@code OrcidTokenRevocationController} (revocation triggered by OSF, e.g. on GDPR delete).
 *
 * Per ORCID's API docs, revoking either the access token or the refresh token revokes the pair, and success is
 * {@code HTTP 200 OK} with an empty body.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
public final class OrcidTokenRevocationClient {

    private static final int CONNECT_AND_SOCKET_TIMEOUT_IN_MILLISECONDS = 5000;

    private OrcidTokenRevocationClient() {
    }

    /**
     * Best-effort revoke a token against ORCID. Never throws; logs and returns {@code false} on any failure.
     *
     * @param revokeUrl ORCID's OAuth revocation endpoint
     * @param clientId CAS's ORCID OAuth client id
     * @param clientSecret CAS's ORCID OAuth client secret
     * @param token the access or refresh token to revoke
     * @return {@code true} if ORCID responded with {@code HTTP 200}, {@code false} otherwise
     */
    public static boolean revoke(
            final String revokeUrl,
            final String clientId,
            final String clientSecret,
            final String token
    ) {
        try {
            final HttpResponse response = Request.Post(revokeUrl)
                    .connectTimeout(CONNECT_AND_SOCKET_TIMEOUT_IN_MILLISECONDS)
                    .socketTimeout(CONNECT_AND_SOCKET_TIMEOUT_IN_MILLISECONDS)
                    .bodyForm(
                            Form.form()
                                    .add("client_id", clientId)
                                    .add("client_secret", clientSecret)
                                    .add("token", token)
                                    .build()
                    )
                    .execute()
                    .returnResponse();
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                LOGGER.debug("Successfully revoked ORCID token against [{}]", revokeUrl);
                return true;
            }
            LOGGER.warn("ORCID token revocation against [{}] returned unexpected status [{}]", revokeUrl, statusCode);
            return false;
        } catch (final IOException e) {
            LOGGER.warn("Failed to revoke ORCID token against [{}]: {}", revokeUrl, e.getMessage());
            return false;
        }
    }
}
