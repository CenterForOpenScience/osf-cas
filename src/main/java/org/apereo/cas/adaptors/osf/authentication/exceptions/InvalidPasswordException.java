package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has failed the password authentication.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class InvalidPasswordException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 7066637925654708862L;

    /**
     * Instantiates a new {@link InvalidPasswordException}.
     *
     * @param msg the msg
     */
    public InvalidPasswordException(final String msg) {
        super(msg);
    }
}
