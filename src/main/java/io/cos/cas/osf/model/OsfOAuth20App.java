package io.cos.cas.osf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link OsfOAuth20App}.
 *
 * @author Longze Chen
 * @since 21.1.0
 */
@Entity
@Table(name = "osf_apioauth2application")
@NoArgsConstructor
@Getter
public final class OsfOAuth20App extends AbstractOsfModel {

    private static final long serialVersionUID = -7719882539457617085L;

    @Column(name = "_id", nullable = false, unique=true)
    private String objectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Override
    public String toString() {
        return String.format("OsfOAuth20App [objectId=%s, name=%s, clientId=%s, callbackUrl=%s]", objectId, clientId, name, callbackUrl);
    }
}
