package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account created via OSF sign-up is not confirmed.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class AccountNotConfirmedOsfException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -2906304335740165483L;

    /**
     * Instantiates a new {@link AccountNotConfirmedOsfException}.
     *
     * @param msg the msg
     */
    public AccountNotConfirmedOsfException(final String msg) {
        super(msg);
    }
}
