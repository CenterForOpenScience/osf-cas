package org.apereo.cas.adaptors.osf.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account created via external IdPs is not confirmed.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class AccountNotConfirmedIdpException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -1890875337422244163L;

    /**
     * Instantiates a new {@link AccountNotConfirmedIdpException}.
     *
     * @param msg the msg
     */
    public AccountNotConfirmedIdpException(final String msg) {
        super(msg);
    }
}
