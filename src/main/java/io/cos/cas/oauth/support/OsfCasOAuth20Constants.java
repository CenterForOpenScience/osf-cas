package io.cos.cas.oauth.support;

/**
 * This class has some extra constants for customized OAuth 2.0 implementation for OSF CAS.
 *
 * See {@link org.apereo.cas.support.oauth.OAuth20Constants} for the main constants in the vanilla OAuth 2.0 implementation by Apereo.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
public interface OsfCasOAuth20Constants {
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
}
