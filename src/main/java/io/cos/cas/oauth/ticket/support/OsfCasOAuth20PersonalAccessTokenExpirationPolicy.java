package io.cos.cas.oauth.ticket.support;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.expiration.AbstractCasExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OsfCasOAuth20PersonalAccessTokenExpirationPolicy}.
 *
 * Personal access token shares the same implementation class {@link org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken} of access
 * token. However, since personal access token never expires, its expiration policy is overridden to return {@code false} without going
 * through the timing checks in {@link org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenExpirationPolicy}.
 *
 * The expiration policy are applied when the personal access token is created from an {@link io.cos.cas.osf.model.OsfOAuth20Pat} with
 * using default properties or ones configured via {@link io.cos.cas.oauth.configuration.model.OsfCasOAuth20PersonalAccessTokenProperties}.
 * However, the properties {@code maxTimeToLiveInSeconds} and {@code timeToKillInSeconds} don't have any effect due to PAT's never expiring.
 *
 * @author Longze Chen
 * @since 21.1.0
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@Slf4j
@ToString(callSuper = true)
public class OsfCasOAuth20PersonalAccessTokenExpirationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = -7144233906843566234L;

    /**
     * Maximum time this token is valid.
     */
    private long maxTimeToLiveInSeconds;

    /**
     * The time to kill in milliseconds.
     */
    private long timeToKillInSeconds;

    /**
     * Instantiates a new {@link OsfCasOAuth20PersonalAccessTokenExpirationPolicy}.
     *
     * @param timeToKillInSeconds the time to kill in seconds
     */
    @JsonCreator
    public OsfCasOAuth20PersonalAccessTokenExpirationPolicy(
            @JsonProperty("timeToLive") final long maxTimeToLiveInSeconds,
            @JsonProperty("timeToIdle") final long timeToKillInSeconds
    ) {
        this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            LOGGER.debug("Personal access token is considered expired because the ticket state is null");
            return true;
        }
        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInSeconds;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }
}
