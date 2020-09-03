package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has failed the verification key authentication.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@NoArgsConstructor
public class InvalidVerificationKeyException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -2419900805378090788L;

    /**
     * Instantiates a new {@link InvalidVerificationKeyException}.
     *
     * @param msg the msg
     */
    public InvalidVerificationKeyException(final String msg) {
        super(msg);
    }
}
