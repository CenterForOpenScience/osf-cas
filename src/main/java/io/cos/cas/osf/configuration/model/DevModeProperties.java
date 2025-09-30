package io.cos.cas.osf.configuration.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link DevModeProperties}.
 *
 * @author Longze Chen
 * @since 25.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class DevModeProperties implements Serializable {

    /**
     * Serialization metadata.
     */
    private static final long serialVersionUID = -1725182183570276203L;

    /**
     * Allow CAS to force throw authentication exceptions and to render respective error pages for testing purpose.
     */
    private boolean allowForceAuthnException = Boolean.FALSE;

    /**
     * Allow CAS to force http errors which have built-in rendering template for rendering and testing.
     */
    private boolean allowForceHttpError = Boolean.FALSE;
}
