package io.cos.cas.osf.authentication.exception;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition when connection failures and/or server errors happen
 * between CAS and OSF API during institution SSO.
 *
 * @author Longze Chen
 * @since 22.1.3
 */
@NoArgsConstructor
public class InstitutionSsoOsfApiFailedException extends AccountException {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -620313210360224932L;

    /**
     * Instantiates a new {@link InstitutionSsoOsfApiFailedException}.
     *
     * @param msg the msg
     */
    public InstitutionSsoOsfApiFailedException(final String msg) {
        super(msg);
    }
}
