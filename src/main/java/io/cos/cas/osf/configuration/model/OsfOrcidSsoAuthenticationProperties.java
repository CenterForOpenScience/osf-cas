package io.cos.cas.osf.configuration.model;

import io.cos.cas.osf.authentication.handler.support.OsfOrcidSsoAuthenticationHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class OsfOrcidSsoAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 4565930696065100663L;

    private String name = OsfOrcidSsoAuthenticationHandler.class.getSimpleName();

    private boolean enabled = Boolean.TRUE;

    private int order;
}
