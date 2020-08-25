package org.apereo.cas.adaptors.osf.authentication.credential;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * OSF Credential.
 *
 * Extends {@link RememberMeUsernamePasswordCredential} to enable non-interactive login with username and verification
 * key via {@link org.apereo.cas.adaptors.osf.web.flow.login.OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfCredential extends RememberMeUsernamePasswordCredential {

    private static final long serialVersionUID = 6991516093569886653L;

    private String verificationKey;

    @Override
    public String getId() {
        return this.getUsername();
    }
}
