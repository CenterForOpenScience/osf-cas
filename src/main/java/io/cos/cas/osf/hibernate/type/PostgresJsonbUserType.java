package io.cos.cas.osf.hibernate.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.hibernate.HibernateException;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This is {@link PostgresJsonbUserType}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
public class PostgresJsonbUserType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class<JsonObject> returnedClass() {
        return JsonObject.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(
            final ResultSet rs,
            final String[] names,
            final SharedSessionContractImplementor session,
            final Object owner
    ) throws HibernateException, SQLException {
        final String jsonString = rs.getString(names[0]);
        if (jsonString == null) {
            return null;
        }
        try {
            return JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (final JsonSyntaxException | IllegalStateException e) {
            throw new RuntimeException("Failed to convert Java JSON String to GSON JsonObject: " + e.getMessage());
        }
    }

    @Override
    public void nullSafeSet(
            final PreparedStatement st,
            final Object value,
            final int index,
            final SharedSessionContractImplementor session
    ) throws HibernateException, SQLException {
        throw new NotYetImplementedException();
    }

    // Immutable object: simply return the argument.
    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    // Objects of this type is immutable.
    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable) this.deepCopy(value);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return this.deepCopy(original);
    }
}
