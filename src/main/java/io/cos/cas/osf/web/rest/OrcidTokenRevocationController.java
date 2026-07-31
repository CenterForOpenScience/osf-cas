package io.cos.cas.osf.web.rest;

import io.cos.cas.osf.authentication.support.OrcidTokenRevocationClient;
import io.cos.cas.osf.dao.OsfOrcidTokenDao;
import io.cos.cas.osf.orcidtoken.OsfOrcidToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * This is {@link OrcidTokenRevocationController}.
 *
 * Handles {@code POST /osf/orcid/revoke}, the endpoint OSF calls (from {@code OSFUser._clear_identifying_information()}
 * on GDPR delete) to ask CAS to revoke a stored ORCID OAuth token. Authenticated via a shared-secret bearer header
 * (a fresh internal-service auth boundary, since an ORCID iD is not itself a secret, unlike client id / client secret
 * pairs used elsewhere).
 *
 * Behavior, regardless of whether a stored token is found: the local row is always removed and {@code HTTP 204} is
 * returned for any successfully processed request (matching "removes regardless of outcome"); {@code 401} is
 * returned only for a missing / invalid shared secret.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class OrcidTokenRevocationController {

    public static final String BASE_URL = "/osf/orcid";

    public static final String REVOKE_URL = BASE_URL + "/revoke";

    private static final String BEARER_PREFIX = "Bearer ";

    private final OsfOrcidTokenDao osfOrcidTokenDao;

    private final String sharedSecret;

    private final String orcidRevokeUrl;

    private final String orcidClientId;

    private final String orcidClientSecret;

    /**
     * Handle a revocation request from OSF.
     *
     * @param authorizationHeader the {@code Authorization: Bearer <secret>} header
     * @param request the request body, expected to carry an {@code orcid_id}
     * @return {@code 401} if the shared secret is missing/invalid, {@code 400} if {@code orcid_id} is missing,
     *         otherwise {@code 204} whether or not a stored token was found
     */
    @PostMapping(path = REVOKE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> revoke(
            @RequestHeader(value = "Authorization", required = false) final String authorizationHeader,
            @RequestBody(required = false) final OrcidTokenRevocationRequest request
    ) {
        if (!isAuthorized(authorizationHeader)) {
            LOGGER.warn("Rejected ORCID token revocation request: missing or invalid shared secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String orcidId = request == null ? null : request.getOrcidId();
        if (StringUtils.isBlank(orcidId)) {
            LOGGER.warn("Rejected ORCID token revocation request: missing orcid_id");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        final OsfOrcidToken token = osfOrcidTokenDao.findByOrcidId(orcidId);
        if (token == null) {
            LOGGER.debug("No stored ORCID token found for ORCID iD [{}]; nothing to revoke.", orcidId);
            return ResponseEntity.noContent().build();
        }
        OrcidTokenRevocationClient.revoke(orcidRevokeUrl, orcidClientId, orcidClientSecret, token.getAccessToken());
        osfOrcidTokenDao.deleteByOrcidId(orcidId);
        LOGGER.info("Revoked and removed stored ORCID token for ORCID iD [{}]", orcidId);
        return ResponseEntity.noContent().build();
    }

    private boolean isAuthorized(final String authorizationHeader) {
        if (StringUtils.isBlank(sharedSecret)
                || StringUtils.isBlank(authorizationHeader)
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return false;
        }
        final String provided = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                sharedSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
