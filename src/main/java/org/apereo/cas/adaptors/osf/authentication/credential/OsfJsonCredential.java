package org.apereo.cas.adaptors.osf.authentication.credential;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OsfJsonCredential}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfJsonCredential extends RememberMeUsernamePasswordCredential {

    private static final long serialVersionUID = 6991516093569886653L;

    @Override
    public String getId() {
        return this.getUsername();
    }
}
