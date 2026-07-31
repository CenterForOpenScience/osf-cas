package io.cos.cas.osf.orcidtoken;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * This is {@link OsfOrcidToken}.
 *
 * Stores the ORCID OAuth access / refresh token captured on ORCID login, keyed by ORCID iD, so that OSF can later ask
 * CAS to revoke it (e.g. on GDPR delete). Deliberately its own class hierarchy rather than a subclass of
 * {@link io.cos.cas.osf.model.AbstractOsfModel}: that base class is tied to the read-only OSF Postgres persistence
 * unit ({@code JpaOsfDaoConfiguration}), whereas this entity lives in the separate, writable persistence unit
 * defined by {@code OrcidTokenJpaConfiguration}.
 *
 * <p>Lives in {@code io.cos.cas.osf.orcidtoken} rather than under {@code io.cos.cas.osf.model} on purpose:
 * {@code JpaOsfDaoConfiguration.jpaOsfDaoModelPackagesToScan()} returns the (undeduplicated) package name of every
 * {@code AbstractOsfModel} subtype it finds, and Spring/Hibernate's package scanning recurses into subpackages — so
 * nesting this under {@code io.cos.cas.osf.model} caused {@link OsfOrcidTokenCryptoConverter} to be swept into the
 * read-only persistence unit's scan too (registered once per duplicate package-name entry), which Hibernate rejects
 * with {@code AttributeConverter class ... registered multiple times}. Keeping this package outside that subtree
 * avoids the collision entirely.</p>
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Entity
@Table(name = "osf_orcid_oauth_token")
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"accessToken", "refreshToken"})
public class OsfOrcidToken implements Serializable {

    private static final long serialVersionUID = 2778546873719340158L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "orcid_id", nullable = false, unique = true)
    private String orcidId;

    @Convert(converter = OsfOrcidTokenCryptoConverter.class)
    @Column(name = "access_token", nullable = false, length = 4096)
    private String accessToken;

    @Convert(converter = OsfOrcidTokenCryptoConverter.class)
    @Column(name = "refresh_token", length = 4096)
    private String refreshToken;

    @Column(name = "scope")
    private String scope;

    @Column(name = "date_created", nullable = false)
    private Date dateCreated;

    @Column(name = "date_modified", nullable = false)
    private Date dateModified;

    @PrePersist
    protected void onCreate() {
        final Date now = new Date();
        this.dateCreated = now;
        this.dateModified = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModified = new Date();
    }
}
