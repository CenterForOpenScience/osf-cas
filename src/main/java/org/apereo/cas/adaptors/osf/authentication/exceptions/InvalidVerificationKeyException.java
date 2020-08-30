package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has failed the verification key authentication.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class InvalidVerificationKeyException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -2419900805378090788L;

    /**
     * Instantiates a new account disabled exception.
     *
     * @param msg the msg
     */
    public InvalidVerificationKeyException(final String msg) {
        super(msg);
    }
}
