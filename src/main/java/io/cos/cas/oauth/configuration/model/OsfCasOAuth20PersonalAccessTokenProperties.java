package io.cos.cas.oauth.configuration.model;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link OsfCasOAuth20PersonalAccessTokenProperties}.
 *
 * This class is used to initialize the {@link io.cos.cas.oauth.ticket.support.OsfCasOAuth20PersonalAccessTokenExpirationPolicy} during
 * the creation of an (person) access token of type {@link io.cos.cas.oauth.model.OsfOAuth20CodeType#PERSONAL}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OsfCasOAuth20PersonalAccessTokenProperties implements Serializable {

    private static final long serialVersionUID = 2622717943331437070L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT28800S";

    /**
     * Sliding window for the access token expiration policy.
     * Essentially, this is an idle time out.
     */
    private String timeToKillInSeconds = "PT7200S";
}
