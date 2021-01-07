package io.cos.cas.osf.authentication.credential;

import io.cos.cas.osf.authentication.support.DelegationProtocol;

import lombok.AllArgsConstructor;
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
 * @since 20.0.0
 */
@AllArgsConstructor
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

    public static String AUTHENTICATION_ATTRIBUTE_TOS_CONSENT = "termsOfServiceChecked";

    private static String DEFAULT_INSTITUTION_ID = "none";

    private static DelegationProtocol DEFAULT_DELEGATION_PROTOCOL = DelegationProtocol.NONE;

    /**
     * The one-time and ephemeral OSF verification key.
     */
    private String verificationKey;

    /**
     * The time-based one-time password (TOTP) for OSF two-factor authentication.
     */
    private String oneTimePassword;

    /**
     * The boolean flag that indicates whether the user has checked the terms of service consent agreement
     */
    private boolean termsOfServiceChecked;

    /**
     * The boolean flag that indicates successful delegated authentication if true.
     */
    private boolean remotePrincipal = Boolean.FALSE;

    /**
     * The ID that indicates which institution it is after successful authentication between CAS / Shib and institutions.
     */
    private String institutionId = DEFAULT_INSTITUTION_ID;

    /**
     * The user's institutional identity when authenticated via institutional SSO.
     */
    private String institutionalIdentity = "";

    /**
     * The authentication delegation protocol that is used between CAS / Shib and institutions.
     */
    private DelegationProtocol delegationProtocol = DEFAULT_DELEGATION_PROTOCOL;

    /**
     * The authentication attributes that are parsed from raw authentication response.
     */
    private Map<String, String> delegationAttributes = new LinkedHashMap<>();

    @Override
    public String getId() {
        return this.getUsername();
    }
}
