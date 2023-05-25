package io.cos.cas.osf.web.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link OsfCasSsoErrorContext}.
 *
 * Stores detailed error information, which can be prepared and put into flow before raising an exception. Extends {@link Serializable}
 * so that it can be put into and retrieved from the flow context conveniently.
 *
 * @author Longze Chen
 * @since 23.2.0
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
@Setter
public class OsfCasSsoErrorContext implements Serializable  {

    private static final long serialVersionUID = -1366351087792035267L;

    private String handleErrorName;

    private String errorMessage;

    private String ssoEmail;

    private String ssoIdentity;

    private String institutionId;

    private String institutionSupportEmail;
}
