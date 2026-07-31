package io.cos.cas.osf.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link OrcidTokenRevocationRequest}, the JSON request body for {@code POST /osf/orcid/revoke}.
 *
 * @author Longze Chen
 * @since 26.1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class OrcidTokenRevocationRequest {

    @JsonProperty("orcid_id")
    private String orcidId;
}
