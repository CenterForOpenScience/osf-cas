package io.cos.cas.osf.model;

import io.cos.cas.osf.hibernate.type.PostgresJsonbUserType;

import com.google.gson.JsonObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * This is {@link OsfUser}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Entity
@Table(name = "osf_osfuser")
@TypeDef(name = "PostgresJsonb", typeClass = PostgresJsonbUserType.class)
@NoArgsConstructor
@Getter
@ToString
public final class OsfUser extends AbstractOsfModel {

    private static final long serialVersionUID = 5634562720139150055L;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    @ToString.Exclude
    private String password;

    @Column(name = "verification_key")
    @ToString.Exclude
    private String verificationKey;

    @Column(name = "given_name", nullable = false)
    private String givenName;

    @Column(name = "family_name", nullable = false)
    private String familyName;

    @Column(name = "is_registered", nullable = false)
    @Getter(AccessLevel.NONE)
    private Boolean registered;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_confirmed")
    private Date dateConfirmed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "accepted_terms_of_service")
    private Date dateTermsOfServiceAccepted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_disabled")
    private Date dateDisabled;

    @ManyToOne
    @JoinColumn(name = "merged_by_id")
    private OsfUser mergedBy;

    @Column(name = "external_identity")
    @Type(type = "PostgresJsonb")
    private JsonObject externalIdentity;

    public Boolean isRegistered() {
        return registered;
    }

    public boolean isMerged() {
        return mergedBy != null;
    }

    public boolean isConfirmed() {
        return dateConfirmed != null;
    }

    public boolean isTermsOfServiceAccepted() {
        return dateTermsOfServiceAccepted != null;
    }

    public boolean isDisabled() {
        return dateDisabled != null;
    }

    public boolean isActive() {
        return isRegistered() && !isMerged() && !isDisabled() && isConfirmed();
    }
}
