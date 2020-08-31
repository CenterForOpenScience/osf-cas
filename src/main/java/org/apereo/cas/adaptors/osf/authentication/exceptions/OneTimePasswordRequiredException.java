package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account requires further two-factor authentication.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class OneTimePasswordRequiredException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 6855949828624301835L;

    /**
     * Instantiates a new {@link OneTimePasswordRequiredException}.
     *
     * @param msg the msg
     */
    public OneTimePasswordRequiredException(final String msg) {
        super(msg);
    }
}
