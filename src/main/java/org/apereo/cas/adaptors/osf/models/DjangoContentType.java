package org.apereo.cas.adaptors.osf.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link DjangoContentType}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Entity
@Table(name = "django_content_type")
@NoArgsConstructor
@Getter
@ToString
@Slf4j
public class DjangoContentType extends AbstractOsfModel {

    private static final long serialVersionUID = 7532814264322554678L;

    @Column(name = "app_label", nullable = false)
    private String appLabel;

    @Column(name = "model", nullable = false)
    private String model;
}