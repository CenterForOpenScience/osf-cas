package org.apereo.cas.adaptors.osf.authentication.credential;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link OsfPostgresCredential}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfPostgresCredential extends RememberMeUsernamePasswordCredential {

    private static final long serialVersionUID = 4705325561237083442L;

    /**
     * The one-time and ephemeral OSF verification key.
     */
    private String verificationKey;

    /**
     * The time-based one-time password (TOTP) for OSF two-factor authentication.
     */
    private String oneTimePassword;

    @Override
    public String getId() {
        return this.getUsername();
    }
}
