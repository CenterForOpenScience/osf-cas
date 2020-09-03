package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * This is {@link AbstractOsfModel}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@MappedSuperclass
@Setter
@ToString
public abstract class AbstractOsfModel implements Serializable {

    private static final long serialVersionUID = -5372017847228934320L;

    @Id
    @Column(name = "id", nullable = false)
    @Getter
    private Integer id;
}
