package io.cos.cas.osf.configuration.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OsfOrcidRevocationProperties}.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsfOrcidRevocationProperties implements Serializable {

    private static final long serialVersionUID = -2836917320958203451L;

    /**
     * ORCID's OAuth token revocation endpoint.
     */
    private String revokeUrl = "https://orcid.org/oauth/revoke";

    /**
     * The shared secret used to authenticate OSF's calls to {@code POST /osf/orcid/revoke}.
     */
    private String sharedSecret;

    /**
     * The symmetric key used to encrypt / decrypt stored ORCID access and refresh tokens at rest.
     */
    private String tokenEncryptionKey;
}
