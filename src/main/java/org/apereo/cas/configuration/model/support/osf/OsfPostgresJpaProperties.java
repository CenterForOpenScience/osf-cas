package org.apereo.cas.configuration.model.support.osf;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OsfPostgresJpaProperties}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Getter
@Setter
@Accessors(chain = true)
public class OsfPostgresJpaProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 2593648298993956294L;

}
