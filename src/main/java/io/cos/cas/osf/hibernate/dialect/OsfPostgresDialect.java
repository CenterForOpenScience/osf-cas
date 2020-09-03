package io.cos.cas.osf.hibernate.dialect;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

/**
 * This is {@link OsfPostgresDialect}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
public class OsfPostgresDialect extends PostgreSQL95Dialect {

    /** The default constructor. */
    public OsfPostgresDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
