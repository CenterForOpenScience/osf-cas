package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link OsfOAuth20PatScopes}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Entity
@Table(name = "osf_apioauth2personaltoken_scopes")
@NoArgsConstructor
@Getter
public class OsfOAuth20PatScopes extends AbstractOsfModel {

    private static final long serialVersionUID = 9057653029543246948L;

    /** The Primary Key of the Personal Access Token Object. */
    @Column(name = "apioauth2personaltoken_id", nullable = false)
    private Integer tokenPk;

    /** The Primary Key of the Scope Object. */
    @Column(name = "apioauth2scope_id", nullable = false)
    private Integer scopePk;

    @Override
    public String toString() {
        return String.format("OsfOAuth20PatScopes [tokenPk=%s, scopePk=%d]", tokenPk, scopePk);
    }
}
