package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has failed the required two-factor authentication.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class InvalidOneTimePasswordException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 7498475667069604263L;

    /**
     * Instantiates a new account disabled exception.
     *
     * @param msg the msg
     */
    public InvalidOneTimePasswordException(final String msg) {
        super(msg);
    }
}
