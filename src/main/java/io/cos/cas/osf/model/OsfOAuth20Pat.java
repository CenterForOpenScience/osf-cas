package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This is {@link OsfOAuth20Pat}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Entity
@Table(name = "osf_apioauth2personaltoken")
@NoArgsConstructor
@Getter
public class OsfOAuth20Pat extends AbstractOsfModel {

    private static final long serialVersionUID = -2951722612257631731L;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private OsfUser owner;

    @Override
    public String toString() {
        return String.format("OsfOAuth20Pat [name=%s, owner=%s]", name, owner.getUsername());
    }
}
