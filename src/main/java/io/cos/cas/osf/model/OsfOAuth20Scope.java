package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link OsfOAuth20Scope}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Entity
@Table(name = "osf_apioauth2scope")
@NoArgsConstructor
@Getter
public class OsfOAuth20Scope extends AbstractOsfModel {

    private static final long serialVersionUID = 2261040960373660257L;

    @Column(name = "_id", nullable = false)
    private String scopeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Override
    public String toString() {
        return String.format("OsfOAuth20Scope [scopeId=%s, name=%s]", scopeId, name);
    }
}
