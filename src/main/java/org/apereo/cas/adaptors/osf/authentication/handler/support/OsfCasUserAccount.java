package org.apereo.cas.adaptors.osf.authentication.handler.support;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OsfCasUserAccount}.
 *
 * Stores information about an OSF user.
 *
 * @author Longze Chen
 * @since 6.2.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
public class OsfCasUserAccount implements Serializable {

    private static final int MAP_SIZE = 8;
    private static final long serialVersionUID = 8765092399074285290L;

    private String password;
    private String verificationKey;
    private Map<String, List<Object>> attributes = new LinkedHashMap<>(MAP_SIZE);
    private AccountStatus status = AccountStatus.OK;
    private LocalDate expirationDate;

    /**
     * Indicates user account status.
     */
    public enum AccountStatus {

        /**
         * Ok account status.
         */
        OK,
        /**
         * Locked account status.
         */
        LOCKED,
        /**
         * Disabled account status.
         */
        DISABLED,
        /**
         * Expired account status.
         */
        EXPIRED,
        /**
         * Must change password account status.
         */
        MUST_CHANGE_PASSWORD
    }
}
