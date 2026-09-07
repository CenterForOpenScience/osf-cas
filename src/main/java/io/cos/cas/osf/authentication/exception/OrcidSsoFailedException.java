package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where ORCiD SSO has failed due to missing id or token(s).
 *
 * @author Longze Chen
 * @since 26.2.0
 */
@NoArgsConstructor
public class OrcidSsoFailedException extends AccountException {

    /** Serial version UID. */
    private static final long serialVersionUID = 7499754046067009352L;

    /**
     * Instantiates a new {@link OrcidSsoFailedException}.
     *
     * @param msg the msg
     */
    public OrcidSsoFailedException(final String msg) {
        super(msg);
    }
}
