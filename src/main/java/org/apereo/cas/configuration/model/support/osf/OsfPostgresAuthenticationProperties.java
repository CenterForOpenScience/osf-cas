package org.apereo.cas.configuration.model.support.osf;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OsfPostgresAuthenticationProperties}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
public class OsfPostgresAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -6126944686676618138L;

    /**
     * The order of the authentication handler.
     */
    private int order;

    /**
     * JPA properties for OSF PostgreSQL database.
     */
    @NestedConfigurationProperty
    private OsfPostgresJpaProperties jpa = new OsfPostgresJpaProperties();
}
