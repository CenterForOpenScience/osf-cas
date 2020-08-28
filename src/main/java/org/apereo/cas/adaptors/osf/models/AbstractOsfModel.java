package org.apereo.cas.adaptors.osf.models;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * This is {@link AbstractOsfModel}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@MappedSuperclass
@NoArgsConstructor
@Setter
@ToString
@Slf4j
public abstract class AbstractOsfModel implements Serializable {

    private static final long serialVersionUID = -5372017847228934320L;

    @Id
    @Column(name = "id", nullable = false)
    @Getter
    private Integer id;
}
