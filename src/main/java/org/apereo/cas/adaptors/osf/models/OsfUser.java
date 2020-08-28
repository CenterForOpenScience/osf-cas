package org.apereo.cas.adaptors.osf.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OsfUser}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Entity
@Table(name = "osf_osfuser")
@NoArgsConstructor
@Getter
@Slf4j
public final class OsfUser extends AbstractOsfModel {

    private static final long serialVersionUID = 5634562720139150055L;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "verification_key")
    private String verificationKey;
}
