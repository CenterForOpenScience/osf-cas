package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where institution SSO is not implemented.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
public class InstitutionSsoNotImplementedException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = 5379752379314379863L;

    /**
     * Instantiates a new {@link InstitutionSsoNotImplementedException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoNotImplementedException(final String msg) {
        super(msg);
    }
}
