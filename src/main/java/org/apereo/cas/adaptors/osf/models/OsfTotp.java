package org.apereo.cas.adaptors.osf.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base32;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;

/**
 * This is {@link OsfTotp}.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@Entity
@Table(name = "addons_twofactor_usersettings")
@NoArgsConstructor
@Getter
@ToString
@Slf4j
public class OsfTotp extends AbstractOsfModel {

    @OneToOne
    @JoinColumn(name = "owner_id")
    private OsfUser owner;

    @Column(name = "totp_secret", nullable = false)
    private String totpSecret;

    @Column(name = "is_confirmed", nullable = false)
    @Getter(AccessLevel.NONE)
    private Boolean confirmed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted")
    private Date deleted;

    private boolean isDeleted() {
        return deleted != null;
    }

    public String getTotpSecretBase32() {
        final byte[] bytes = DatatypeConverter.parseHexBinary(totpSecret);
        return new Base32().encodeAsString(bytes);
    }

    public boolean isActive() {
        return totpSecret != null && confirmed && !isDeleted();
    }
}
