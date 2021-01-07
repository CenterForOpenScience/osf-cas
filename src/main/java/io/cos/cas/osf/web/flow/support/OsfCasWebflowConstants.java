package io.cos.cas.osf.web.flow.support;

/**
 * This is {@link OsfCasWebflowConstants}, which expands the default {@link org.apereo.cas.web.flow.CasWebflowConstants}
 * interface by adding OSF CAS customized action, state, transition and view IDs as well as names web flow parameters.
 *
 * @author Longze Chen
 * @since 20.0.0
 */

public interface OsfCasWebflowConstants {

    String FLOW_PARAMETER_OSF_URL = "osfUrl";

    String ACTION_ID_OSF_PRE_INITIAL_FLOW_SETUP = "osfCasPreInitialFlowSetupAction";

    String ACTION_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheckAction";

    String ACTION_ID_OSF_DEFAULT_LOGIN_CHECK = "osfDefaultLoginCheckAction";

    String ACTION_ID_OSF_INSTITUTION_LOGIN_CHECK = "osfInstitutionLoginCheckAction";

    String TRANSITION_ID_USERNAME_PASSWORD_LOGIN = "continueToUsernamePasswordLogin";

    String TRANSITION_ID_INSTITUTION_LOGIN = "switchToInstitutionLogin";

    String TRANSITION_ID_ORCID_LOGIN_AUTO_REDIRECT = "autoRedirectToOrcidLogin";

    String TRANSITION_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT = "autoRedirectToDefaultServiceLogin";

    String STATE_ID_OSF_NON_INTERACTIVE_AUTHENTICATION_CHECK = "osfNonInteractiveAuthenticationCheck";

    String STATE_ID_OSF_DEFAULT_LOGIN_CHECK = "osfDefaultLoginCheck";

    String STATE_ID_OSF_INSTITUTION_LOGIN_CHECK = "osfInstitutionLoginCheck";

    String VIEW_ID_INSTITUTION_SSO_INIT = "casInstitutionLoginView";

    String VIEW_ID_ORCID_LOGIN_AUTO_REDIRECT = "casAutoRedirectToOrcidLoginView";

    String VIEW_ID_DEFAULT_SERVICE_LOGIN_AUTO_REDIRECT = "casAutoRedirectToDefaultServiceLoginView";

    String VIEW_ID_ONE_TIME_PASSWORD_REQUIRED = "casTwoFactorLoginView";

    String VIEW_ID_TERMS_OF_SERVICE_CONSENT_REQUIRED = "casTermsOfServiceConsentView";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_OSF = "casAccountNotConfirmedOsfView";

    String VIEW_ID_ACCOUNT_NOT_CONFIRMED_IDP = "casAccountNotConfirmedIdPView";

    String VIEW_ID_INVALID_USER_STATUS = "casInvalidUserStatusView";

    String VIEW_ID_INVALID_VERIFICATION_KEY = "casInvalidVerificationKeyView";

    String VIEW_ID_INSTITUTION_SSO_FAILED = "casInstitutionSsoFailedView";
}
