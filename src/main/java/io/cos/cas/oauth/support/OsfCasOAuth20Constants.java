package io.cos.cas.oauth.support;

/**
 * This class has some extra constants for customized OAuth 2.0 implementation for OSF CAS.
 *
 * See {@link org.apereo.cas.support.oauth.OAuth20Constants} for the main constants in the vanilla OAuth 2.0 implementation by Apereo.
 *
 * @author Longze Chen
 * @since 21.1.0
 */
public interface OsfCasOAuth20Constants {

    /**
     * The parameter name for OAuth 2.0 error code.
     */
    String ERROR_NAME = "errorCode";

    /**
     * The parameter name for OAuth 2.0 error message.
     */
    String ERROR_MSG = "errorMsg";

    /**
     * The parameter name for the request parameter that has caused the OAuth 2.0 error.
     */
    String ERROR_PARAM = "errorParam";

    /**
     * Default OAuth 2.0 error message.
     */
    String DEFAULT_ERROR_MSG = "The authorization request has failed due to invalid request parameters.";

    /**
     * Request not authenticated.
     */
    String NOT_AUTHENTICATED = "not_authenticated";

    /**
     * Missing request parameter.
     */
    String MISSING_PARAM = "missing_request_param";

    /**
     * Invalid or not-allowed request parameter.
     */
    String PARAM_NOT_ALLOWED = "request_param_not_allowed";

    /**
     * Invalid response type.
     */
    String INVALID_RESP_TYPE = "invalid_or_empty_resp_type";

    /**
     * Response type is not allowed for a given registered service.
     */
    String RESP_TYPE_NOT_ALLOWED = "resp_type_not_allowed";

    /**
     * The registered service is not allowed.
     */
    String SERVICE_NOT_ALLOWED = "service_not_allowed";

    /**
     * Invalid redirect / callback URI.
     */
    String INVALID_REDIRECT_URI = "invalid_redirect_url";

    /**
     * Validator not found.
     */
    String VALIDATOR_NOT_FOUND = "validator_not_found";

    /**
     * OSF URL.
     */
    String OSF_URL = "osfUrl";

    /**
     * The access type.
     */
    String ACCESS_TYPE = "access_type";

    /**
     * The oldCAS approval prompt param.
     */
    String APPROVAL_PROMPT = "approval_prompt";

    /**
     * The oldCAS approval prompt value "auto".
     */
    String APPROVAL_PROMPT_AUTO = "auto";

    /**
     * The oldCAS approval prompt value "force".
     */
    String APPROVAL_PROMPT_FORCE= "force";

    /**
     * Not implemented.
     */
    String NOT_IMPLEMENTED = "not_implemented";

    /**
     * Internal Server Error.
     */
    String INTERNAL_SERVER_ERROR = "internal_server_error";

    /**
     * OAuth 2.0 internal server error message.
     */
    String SERVER_ERROR_MSG = "The authorization request has failed due to an unexpected server error.";

    /**
     *  Stale or mismatched session request.
     */
    String SESSION_STALE_MISMATCH = "session_stale_mismatch";
}
