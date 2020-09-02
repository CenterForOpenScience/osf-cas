package io.cos.cas.osf.web.flow.support;

/**
 * This is {@link OsfCasWebflowConstants}, which expands the default {@link org.apereo.cas.web.flow.CasWebflowConstants}
 * interface by adding OSF CAS customized action, state and view IDs.
 *
 * @author Longze Chen
 * @since 6.2.1
 */

public interface OsfCasWebflowConstants {

    String ACTION_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheckAction";

    String STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheck";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF = "casAccountNotConfirmedOsfView";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP = "casAccountNotConfirmedIdPView";

    String VIEW_ID_INVALID_USER_STATUS = "casInvalidUserStatusView";

    String VIEW_ID_INVALID_VERIFICATION_KEY = "casInvalidVerificationKeyView";

    String VIEW_ID_ONE_TIME_PASSWORD_REQUIRED = "casTwoFactorLoginView";

    String VIEW_ID_INSTITUTION_SSO_NOT_IMPLEMENTED = "casInstitutionSsoNotImplementedView";
}
