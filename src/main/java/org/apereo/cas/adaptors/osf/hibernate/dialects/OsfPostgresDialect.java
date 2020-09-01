package org.apereo.cas.adaptors.osf.hibernate.dialects;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

/**
 * This is {@link OsfPostgresDialect}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
public class OsfPostgresDialect extends PostgreSQL95Dialect {

    /** The default constructor. */
    public OsfPostgresDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
