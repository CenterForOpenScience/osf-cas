package io.cos.cas.osf.authentication.credential;

import io.cos.cas.osf.authentication.support.DelegationProtocol;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public static String AUTHENTICATION_ATTRIBUTE_INSTITUTION_ID = "institutionId";

    public static String AUTHENTICATION_ATTRIBUTE_REMOTE_PRINCIPAL = "remotePrincipal";

    public static String AUTHENTICATION_ATTRIBUTE_DELEGATION_PROTOCOL = "delegationProtocol";

    public static String AUTHENTICATION_ATTRIBUTE_REMEMBER_ME = "rememberMe";

    private static String DEFAULT_INSTITUTION_ID = "none_osf";

    private static DelegationProtocol DEFAULT_DELEGATION_PROTOCOL = DelegationProtocol.NONE_OSF;

    /**
     * The one-time and ephemeral OSF verification key.
     */
    private String verificationKey;

    /**
     * The time-based one-time password (TOTP) for OSF two-factor authentication.
     */
    private String oneTimePassword;

    private boolean remotePrincipal = Boolean.FALSE;

    private String institutionId = DEFAULT_INSTITUTION_ID;

    private DelegationProtocol delegationProtocol = DEFAULT_DELEGATION_PROTOCOL;

    private Map<String, String> delegationAttributes = new LinkedHashMap<>();

    @Override
    public String getId() {
        return this.getUsername();
    }
}
