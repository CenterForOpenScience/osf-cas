package io.cos.cas.oauth.ticket.accesstoken;

/**
 * This is {@link OsfCasOAuth20PersonalAccessToken}.
 *
 * Due to the fact that current CAS personal access token shares the implementation with CAS vanilla access token,
 * this interface has not been implemented and thus serves as "constant" object that stores the ticket prefix.
 *
 * Refer to {@link org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController} and
 * {@link org.apereo.cas.config.OAuth20ProtocolTicketCatalogConfiguration} for usages of this prefix and the reason
 * why it preferred / required.
 *
 * @author Longze Chen
 * @since 21.1.0
 */
public interface OsfCasOAuth20PersonalAccessToken {

    /**
     * The prefix for personal access tokens.
     */
    String PREFIX = "OSFPAT";
}
