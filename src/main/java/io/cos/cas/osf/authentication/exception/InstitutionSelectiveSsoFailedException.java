package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where user is not allowed to access OSF via institution SSO.
 *
 * @author Longze Chen
 * @since 22.0.1
 */
@NoArgsConstructor
public class InstitutionSelectiveSsoFailedException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -7613915260905373074L;

    /**
     * Instantiates a new {@link InstitutionSelectiveSsoFailedException}.
     *
     * @param msg the msg
     */
    public InstitutionSelectiveSsoFailedException(final String msg) {
        super(msg);
    }
}
