package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has an unexpected invalid status.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@NoArgsConstructor
public class InvalidUserStatusException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 5702991703923618882L;

    /**
     * Instantiates a new {@link InvalidUserStatusException}.
     *
     * @param msg the msg
     */
    public InvalidUserStatusException(final String msg) {
        super(msg);
    }
}
