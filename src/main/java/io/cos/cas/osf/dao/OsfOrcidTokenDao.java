package io.cos.cas.osf.dao;

import io.cos.cas.osf.orcidtoken.OsfOrcidToken;

/**
 * This is {@link OsfOrcidTokenDao}.
 *
 * DAO for the writable {@code osf_orcid_oauth_token} table, used to capture ORCID OAuth tokens on login and to
 * support CAS-side revocation triggered by OSF (e.g. GDPR delete).
 *
 * @author Longze Chen
 * @since 26.1.0
 */
public interface OsfOrcidTokenDao {

    /**
     * Find the stored token for a given ORCID iD, if any.
     *
     * @param orcidId the ORCID iD
     * @return the stored token, or {@code null} if none exists
     */
    OsfOrcidToken findByOrcidId(String orcidId);

    /**
     * Insert or update the stored token for a given ORCID iD.
     *
     * @param orcidId the ORCID iD (natural key)
     * @param accessToken the ORCID OAuth access token
     * @param refreshToken the ORCID OAuth refresh token, may be {@code null}
     * @param scope the granted OAuth scope, may be {@code null}
     * @return the persisted token entity
     */
    OsfOrcidToken upsertToken(String orcidId, String accessToken, String refreshToken, String scope);

    /**
     * Delete the stored token for a given ORCID iD, if any. A no-op if none exists.
     *
     * @param orcidId the ORCID iD
     */
    void deleteByOrcidId(String orcidId);
}
