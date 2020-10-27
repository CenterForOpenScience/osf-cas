package io.cos.cas.osf.web.flow.support;

/**
 * This is {@link OsfCasWebflowConstants}, which expands the default {@link org.apereo.cas.web.flow.CasWebflowConstants}
 * interface by adding OSF CAS customized action, state and view IDs.
 *
 * @author Longze Chen
 * @since 20.0.0
 */

public interface OsfCasWebflowConstants {

    String ACTION_ID_OSF_DEFAULT_LOGIN_CHECK = "osfDefaultLoginCheckAction";

    String STATE_ID_OSF_DEFAULT_LOGIN_CHECK = "osfDefaultLoginCheck";

    String TRANSITION_ID_USERNAME_PASSWORD_LOGIN = "continueToUsernamePasswordLogin";

    String ACTION_ID_OSF_INSTITUTION_LOGIN_CHECK = "osfInstitutionLoginCheckAction";

    String STATE_ID_OSF_INSTITUTION_LOGIN_CHECK = "osfInstitutionLoginCheck";

    String TRANSITION_ID_INSTITUTION_LOGIN = "switchToInstitutionLogin";

    String TRANSITION_ID_ORCID_LOGIN_AUTO_REDIRECT = "autoRedirectToOrcidLogin";

    String VIEW_ID_ORCID_LOGIN_AUTO_REDIRECT = "casAutoRedirectToOrcidLoginView";

    String ACTION_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheckAction";

    String STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheck";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF = "casAccountNotConfirmedOsfView";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP = "casAccountNotConfirmedIdPView";

    String VIEW_ID_INVALID_USER_STATUS = "casInvalidUserStatusView";

    String VIEW_ID_INVALID_VERIFICATION_KEY = "casInvalidVerificationKeyView";

    String VIEW_ID_ONE_TIME_PASSWORD_REQUIRED = "casTwoFactorLoginView";

    String VIEW_ID_INSTITUTION_SSO_NOT_IMPLEMENTED = "casInstitutionSsoNotImplementedView";
}
