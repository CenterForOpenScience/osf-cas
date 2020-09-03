package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This is {@link OsfEmail}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Entity
@Table(name = "osf_email")
@NoArgsConstructor
@Getter
@ToString
public final class OsfEmail extends AbstractOsfModel {

    private static final long serialVersionUID = -1608581129054050883L;

    @Column(name = "address", nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private OsfUser user;
}
