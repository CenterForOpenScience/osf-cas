package io.cos.cas.osf.configuration.model;

import io.cos.cas.osf.authentication.handler.support.OsfOrcidSsoAuthenticationHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OsfOrcidSsoAuthenticationProperties}.
 *
 * @author Longze Chen
 * @since 26.2.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsfOrcidSsoAuthenticationProperties implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 4565930696065100663L;

    /** The name of the authentication handler. */
    private String name = OsfOrcidSsoAuthenticationHandler.class.getSimpleName();

    /** The flag to enable / disable the authentication handler. */
    private boolean enabled = Boolean.TRUE;

    /** The order of the authentication handler. */
    private int order;
}
