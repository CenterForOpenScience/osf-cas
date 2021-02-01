package io.cos.cas.osf.web.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link OsfApiInstitutionAuthenticationResult}.
 *
 * @author Longze Chen
 * @since 21.0.0
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
@Setter
public class OsfApiInstitutionAuthenticationResult implements Serializable {

    private static final long serialVersionUID = 3971349776123204760L;

    private String username;

    private String institutionId;
}
