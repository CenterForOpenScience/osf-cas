package io.cos.cas.osf.authentication.credential;

import org.apereo.cas.authentication.credential.AbstractCredential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OsfOrcidSsoCredential extends AbstractCredential {

    private static final long serialVersionUID = 7983138918562300147L;

    public static final String CREDENTIAL_ID_PREFIX = "OrcidProfile#";

    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ID = "orcidId";

    public static final String AUTHENTICATION_ATTRIBUTE_ORCID_ACCESS_TOKEN = "orcidAccessToken";

    private String orcidId;

    private String orcidAccessToken;

    @Override
    public String getId() {
        return CREDENTIAL_ID_PREFIX + this.getOrcidId();
    }
}
