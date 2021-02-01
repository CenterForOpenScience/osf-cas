package io.cos.cas.osf.model;

import io.cos.cas.osf.authentication.support.DelegationProtocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link OsfInstitution}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@Entity
@Table(name = "osf_institution")
@NoArgsConstructor
@Getter
@ToString
public class OsfInstitution extends AbstractOsfModel {

    private static final long serialVersionUID = -1186000864052788250L;

    @Column(name = "_id")
    private String institutionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "login_url")
    private String loginUrl;

    @Column(name = "logout_url")
    private String logoutUrl;

    @Column(name = "delegation_protocol")
    private String delegationProtocol;

    @Column(name = "is_deleted")
    private Boolean deleted;

    public DelegationProtocol getDelegationProtocol() {
        try {
            return DelegationProtocol.getType(delegationProtocol);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
