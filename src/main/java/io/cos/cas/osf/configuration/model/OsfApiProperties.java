package io.cos.cas.osf.configuration.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OsfApiProperties}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsfApiProperties implements Serializable {

    private static final long serialVersionUID = 427830234394415772L;

    /**
     * The institution authentication endpoint of OSF API.
     */
    private String instnAuthnEndpoint;

    /**
     * The secret that is used for signing the JWT claim.
     */
    private String instnAuthnJwtSecret;

    /**
     * The secret that is used for encrpyting the signed JWT claim.
     */
    private String instnAuthnJweSecret;

    /**
     * The location of the XSL file that is used for transforming XML authentication responses.
     */
    private String instnAuthnXslLocation;
}
