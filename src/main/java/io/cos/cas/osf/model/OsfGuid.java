package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * This is {@link OsfGuid}.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Entity
@Table(name = "osf_guid")
@NoArgsConstructor
@Getter
@ToString
public class OsfGuid extends AbstractOsfModel {

    private static final long serialVersionUID = 5782669366012776490L;

    @Column(name = "_id", nullable = false)
    private String guid;

    @Column(name = "object_id", nullable = false)
    private Integer objectId;

    @ManyToOne
    @JoinColumn(name = "content_type_id")
    private DjangoContentType djangoContentType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;
}
