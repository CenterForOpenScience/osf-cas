package io.cos.cas.osf.web.flow.login;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.authentication.exception.InstitutionSsoAccountInactiveException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoAttributeMissingException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoAttributeParsingException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoDuplicateIdentityException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoFailedException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoSelectiveLoginDeniedException;
import io.cos.cas.osf.authentication.exception.InstitutionSsoOsfApiFailedException;
import io.cos.cas.osf.authentication.support.DelegationProtocol;
import io.cos.cas.osf.authentication.support.OsfApiPermissionDenied;
import io.cos.cas.osf.authentication.support.OsfInstitutionUtils;
import io.cos.cas.osf.configuration.model.OsfApiProperties;
import io.cos.cas.osf.configuration.model.OsfUrlProperties;
import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.web.support.OsfApiInstitutionAuthenticationResult;
import io.cos.cas.osf.web.support.OsfCasSsoErrorContext;

import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;

import org.apache.http.util.EntityUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.json.JSONObject;
import org.json.XML;

import org.springframework.util.ResourceUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * Extends {@link AbstractNonInteractiveCredentialsAction} to check if there is any non-interactive authentication
 * available. There are five different cases depends on whether there is an active client credential, whether there
 * is a valid shibboleth authentication session, and whether there is a pair of username and verification key in
 * request parameters. Credentials are checked first, then the shibboleth session, and finally the verification key.
 *
 * 1) In the case of non-institution pac4j authentication delegation (e.g. ORCiD), if credential with a matching
 * client is found, simply use that credential and return the {@link AbstractAction#success()} event.
 *
 * For 1) only, the success event will trigger authentication with pac4j's authentication handler.
 *
 * 2) In the case of institution pac4j authentication delegation (e.g. Concordia), if credential with a
 * matching client is found, extract the client info, principal ID and authentication attributes and store them into
 * the {@code OsfPostgresCredential#delegationAttributes} object.
 *
 * 3) In the case of institution shibboleth authentication delegation (e.g. the rest of the institutions), if a valid
 * shibboleth session (by checking the {@code AUTH-Shib-Session-Id} header) head is found, extract all AUTH headers
 * and store them into the {@code OsfPostgresCredential#delegationAttributes} object.
 *
 * For both 2) and 3), OSF CAS then applies an XLS transformation to validate and convert delegation attributes into
 * a JSON object using the "instn-authn.xsl" file. JWT sign andJWE encrypt the JSON object, and use it as the payload
 * to authenticate against the OSF API institution authentication endpoint. If successful, update the credential and
 * return the {@link AbstractAction#success()} event.
 *
 * 4) In the case of username and verification key login, if both found in requests parameters, store them into the
 * {@link OsfPostgresCredential} and return the {@link AbstractAction#success()} event.
 *
 * For 2), 3) and 4), the success event will trigger authentication using the {@code OsfPostgresAuthenticationHandler}.
 *
 * 5) If none of the credential, shibboleth session or the pair of username and verification key are found, return the
 * {@link AbstractAction#error()} event so that the login web flow will go to the pre-login check state and prepare for
 * the username / password login form, ORCiD login button and institution login page.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Slf4j
@Setter
@Getter
public class OsfPrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    private static final String PARAMETER_SSO_ERROR_CONTEXT = "osfCasSsoErrorContext";

    private static final String USERNAME_PARAMETER_NAME = "username";

    private static final String VERIFICATION_KEY_PARAMETER_NAME = "verification_key";

    private static final String FORCE_EXCEPTION_PARAMETER_NAME = "forceException";

    private static final String OSF_URL_FLOW_PARAMETER = "osfUrl";

    private static final String AUTHENTICATION_EXCEPTION = "authnError";

    private static final int SIXTY_SECONDS = 60 * 1000;

    public static final String INSTITUTION_CLIENTS_PARAMETER_NAME = "institutionClients";

    public static final String NON_INSTITUTION_CLIENTS_PARAMETER_NAME = "nonInstitutionClients";

    private static final String REMOTE_USER = "remote_user";

    private static final String ATTRIBUTE_PREFIX = "auth-";

    private static final String SHIBBOLETH_SESSION_HEADER = ATTRIBUTE_PREFIX + "shib-session-id";

    private static final String SHIBBOLETH_COOKIE_PREFIX = "_shibsession_";

    private static final String LDAP_DN_OU_PREFIX = "ou=";

    private static final int OSF_API_RETRY_LIMIT = 3;

    private static final List<Integer> OSF_API_RETRY_STATUS = List.of(
            HttpStatus.SC_INTERNAL_SERVER_ERROR,
            HttpStatus.SC_BAD_GATEWAY,
            HttpStatus.SC_SERVICE_UNAVAILABLE,
            HttpStatus.SC_GATEWAY_TIMEOUT
    );

    private static final int OSF_API_RETRY_DELAY_IN_SECONDS = 1;

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    private final JpaOsfDao jpaOsfDao;

    @NotNull
    private OsfUrlProperties osfUrlProperties;

    @NotNull
    private OsfApiProperties osfApiProperties;

    @NotNull
    private Map<String, List<String>> authnDelegationClients;

    private Transformer instnAuthnRespTransformer;

    public OsfPrincipalFromNonInteractiveCredentialsAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final CentralAuthenticationService centralAuthenticationService,
            final JpaOsfDao jpaOsfDao,
            final OsfUrlProperties osfUrlProperties,
            final OsfApiProperties osfApiProperties,
            final Map<String, List<String>> authnDelegationClients
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
        this.centralAuthenticationService = centralAuthenticationService;
        this.jpaOsfDao = jpaOsfDao;
        this.osfUrlProperties = osfUrlProperties;
        this.osfApiProperties = osfApiProperties;
        this.authnDelegationClients = authnDelegationClients;
    }

    @SneakyThrows
    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final Credential credential = WebUtils.getCredential(context);

        // Check 1: is there an existing credential?
        if (credential != null) {
            LOGGER.debug("Existing credential found in context of type [{}]", credential.getClass());
            if (credential instanceof ClientCredential) {
                final String clientName = ((ClientCredential) credential).getClientName();
                // Type 1: non-institution SSO (i.e. ORCiD) via pac4j authentication delegation using the OAuth protocol
                if (authnDelegationClients.get(NON_INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid non-institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    return credential;
                }
                // Type 2: institution SSO via pac4j authentication delegation using the CAS protocol
                if (authnDelegationClients.get(INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    final OsfPostgresCredential osfPostgresCredential = constructCredentialsFromPac4jAuthentication(context, clientName);
                    if (osfPostgresCredential != null) {
                        final OsfApiInstitutionAuthenticationResult remoteUserInfo
                                = notifyOsfApiOfInstnAuthnSuccess(context, osfPostgresCredential);
                        osfPostgresCredential.setUsername(remoteUserInfo.getSsoEmail());
                        osfPostgresCredential.setInstitutionId(remoteUserInfo.getInstitutionId());
                        WebUtils.removeCredential(context);
                        return osfPostgresCredential;
                    }
                    LOGGER.error("osfPostgresCredential is null for client [{}]", clientName);
                    return null;
                }
                LOGGER.error("Unsupported delegation client [{}]", clientName);
                return null;
            }
            LOGGER.error("Unexpected credential of type [{}]", credential.getClass().getSimpleName());
            return null;
        }
        LOGGER.debug("No valid client credential found in the request context: check shibboleth session.");

        // Check 2: is there an existing Shibboleth session?
        final String shibbolethSession = request.getHeader(SHIBBOLETH_SESSION_HEADER);
        if (StringUtils.isNotBlank(shibbolethSession)) {
            // Type 3: institution sso via Shibboleth authentication using the SAML protocol
            LOGGER.debug("Shibboleth session / header found in request context.");
            final OsfPostgresCredential osfPostgresCredential = constructCredentialsFromShibbolethAuthentication(context, request);
            final OsfApiInstitutionAuthenticationResult remoteUserInfo
                    = notifyOsfApiOfInstnAuthnSuccess(context, osfPostgresCredential);
            final String ssoIdentity = osfPostgresCredential.getSsoIdentity();
            final String eppn = osfPostgresCredential.getDelegationAttributes().get("eppn");
            final String mail = osfPostgresCredential.getDelegationAttributes().get("mail");
            final String mailOther = osfPostgresCredential.getDelegationAttributes().get("mailother");
            if (!remoteUserInfo.verifyOsfSsoEmail(eppn, mail, mailOther)) {
                LOGGER.error(
                        "[SAML Shibboleth] Critical Error: ssoIdentity={}, ssoEmail={}, institutionId={}, eppn={}, mail={}, mailOther={}",
                        ssoIdentity,
                        remoteUserInfo.getSsoEmail(),
                        remoteUserInfo.getInstitutionId(),
                        eppn,
                        mail,
                        mailOther
                );
                throw new InstitutionSsoFailedException("Critical SAML-Shibboleth SSO Failure");
            }
            // Note: OsfPostgresCredential.username isn't necessarily the OSF user's username. It can be any of the user's emails.
            osfPostgresCredential.setUsername(remoteUserInfo.getSsoEmail());
            osfPostgresCredential.setInstitutionId(remoteUserInfo.getInstitutionId());
            if (StringUtils.isBlank(ssoIdentity)) {
                LOGGER.warn(
                        "[SAML Shibboleth] OSF Postgres Credential created w/o identity: ssoEmail={}, institutionId={}",
                        osfPostgresCredential.getUsername(),
                        osfPostgresCredential.getInstitutionId()
                );
            }
            LOGGER.info(
                    "[SAML Shibboleth] OSF Postgres Credential created w/ identity: ssoEmail={}, institution={}, ssoIdentity={}",
                    osfPostgresCredential.getUsername(),
                    osfPostgresCredential.getInstitutionId(),
                    ssoIdentity
            );
            return osfPostgresCredential;
        }
        LOGGER.debug("No valid shibboleth session found in request context: check username and verification key.");

        // Check 3: is there a pair of username and verification key in the request?
        final String username = request.getParameter(USERNAME_PARAMETER_NAME);
        final String verificationKey = request.getParameter(VERIFICATION_KEY_PARAMETER_NAME);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(verificationKey)) {
            // Type 4: automatic login with short-lived one-time verification key
            return constructCredentialsFromUsernameAndVerificationKey(username, verificationKey);
        }
        LOGGER.debug("No valid username or verification key found in request parameters.");

        // Check 4: check "forceException=" query parameter
        final String forcedException = request.getParameter(FORCE_EXCEPTION_PARAMETER_NAME);
        if (StringUtils.isNotBlank(forcedException)) {
            setSsoErrorContext(
                    context,
                    forcedException,
                    String.format("This exception was thrown on purpose bypassing standard web flow: %s", Class.forName(forcedException).getSimpleName()),
                    "N/A",
                    "N/A",
                    "N/A",
                    "N/A"
            );
            try {
                throw (LoginException) Class.forName(forcedException).getConstructor(String.class).newInstance(FORCE_EXCEPTION_PARAMETER_NAME);
            } catch ( java.lang.ClassCastException e) {
                throw (RuntimeException) Class.forName(forcedException).getConstructor(String.class).newInstance(FORCE_EXCEPTION_PARAMETER_NAME);
            }
        }

        // Default when there is no non-interactive authentication available
        // Type 5: return a null credential so that the login webflow will prepare login pages
        return null;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        // super.doPreExecute() calls constructCredentialsFromRequest() whose exception must be caught and returned as a flow event
        try {
            return super.doPreExecute(context);
        } catch (AccountException e) {
            return new Event(
                    this,
                    CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    new LocalAttributeMap<>(CasWebflowConstants.TRANSITION_ID_ERROR, new AuthenticationException(e))
            );
        }
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return super.doExecute(requestContext);
    }

    /**
     * On error.
     *
     * Super class {@link AbstractNonInteractiveCredentialsAction} always appends an error message to the message
     * context when returning error, of which the default one is{@link javax.security.auth.login.FailedLoginException}.
     * This leads to an extra error message being displayed along with the actual error message when exception happens.
     * Thus, must override the {@link AbstractNonInteractiveCredentialsAction#onError(RequestContext)} with no-op.
     *
     * @param context the context
     */
    @Override
    protected void onError(final RequestContext context) {}

    /**
     * This method allows the bean instance to perform validation of its overall configuration and final initialization
     * when all bean properties have been set.
     *
     * @throws Exception in the event of mis-configuration or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final File xslFile = ResourceUtils.getFile(osfApiProperties.getInstnAuthnXslLocation());
        final StreamSource xslStreamSource = new StreamSource(xslFile);
        final TransformerFactory tFactory = TransformerFactory.newInstance();
        instnAuthnRespTransformer = tFactory.newTransformer(xslStreamSource);
        super.afterPropertiesSet();
    }

    /**
     * Construct an {@link OsfPostgresCredential} object form pac4j delegated authentication via the CAS client.
     *
     * @param context the request context
     * @param clientName the client name
     * @return an {@link OsfPostgresCredential} object
     */
    private OsfPostgresCredential constructCredentialsFromPac4jAuthentication(final RequestContext context, final String clientName) {

        final Authentication authentication = WebUtils.getAuthentication(context);
        final Principal principal = authentication.getPrincipal();
        final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
        osfPostgresCredential.setRemotePrincipal(Boolean.TRUE);
        osfPostgresCredential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
        osfPostgresCredential.getDelegationAttributes().put("cas-identity-provider", clientName);
        if (principal.getAttributes().size() > 0) {
            for (final Map.Entry<String, List<Object>> entry : principal.getAttributes().entrySet()) {
                final String attributeKey = entry.getKey();
                final List<Object> attributeValues = entry.getValue();
                if (attributeValues.isEmpty()) {
                    LOGGER.error(
                            "[CAS PAC4J] Empty-value attribute detected: '{}', '{}', '{}', '{}'",
                            clientName,
                            principal.getId(),
                            attributeKey,
                            attributeValues
                    );
                } else if (attributeValues.size() > 1) {
                    LOGGER.error(
                            "[CAS PAC4J] Multi-value attribute detected: '{}', '{}', '{}', '{}'",
                            clientName,
                            principal.getId(),
                            attributeKey,
                            attributeValues
                    );
                } else {
                    final Object firstAttributeValue = attributeValues.get(0);
                    LOGGER.debug(
                            "[CAS PAC4J] User's institutional identity '{}': '{}' w/ attribute '{}': '{}'",
                            clientName,
                            principal.getId(),
                            attributeKey,
                            firstAttributeValue
                    );
                    if (firstAttributeValue instanceof String) {
                        LOGGER.info(
                                "[CAS PAC4J] Delegation attribute map updated: '{}', '{}', '{}', '{}'",
                                clientName,
                                principal.getId(),
                                attributeKey,
                                firstAttributeValue
                        );
                        osfPostgresCredential.getDelegationAttributes().put(
                                attributeKey,
                                String.valueOf(firstAttributeValue)
                        );
                    } else {
                        LOGGER.error(
                                "[CAS PAC4J] Attribute w/ non-string value: '{}', '{}', '{}', '{}', '{}'",
                                clientName,
                                principal.getId(),
                                entry.getKey(),
                                attributeKey,
                                firstAttributeValue.getClass().getName()
                        );
                    }
                }
            }
            return osfPostgresCredential;
        } else {
            LOGGER.error("[CAS PAC4J] No attributes for user '{} with client '{}'", principal.getId(), clientName);
            return null;
        }
    }

    /**
     * Construct an {@link OsfPostgresCredential} object from Shibboleth authentication.
     *
     * @param context the request context
     * @param request the shibboleth authentication request with "auth-" prefixed headers
     * @return an {@link OsfPostgresCredential} object
     */
    private OsfPostgresCredential constructCredentialsFromShibbolethAuthentication(
            final RequestContext context,
            final HttpServletRequest request
    ) {
        final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
        osfPostgresCredential.setDelegationProtocol(DelegationProtocol.SAML_SHIB);
        osfPostgresCredential.setRemotePrincipal(Boolean.TRUE);
        removeShibbolethSessionCookie(context);

        // The request header REMOTE_USER stores the value for SSO user's institutional identity
        String remoteUser = request.getHeader(REMOTE_USER);
        if (remoteUser != null) {
            remoteUser = remoteUser.trim();
        }
        if (StringUtils.isBlank(remoteUser)) {
            LOGGER.warn("[SAML Shibboleth] Missing or empty Shibboleth header [{}] for SSO identity", REMOTE_USER);
        } else {
            osfPostgresCredential.setSsoIdentity(remoteUser);
            LOGGER.info("[SAML Shibboleth] SSO identity [{}] found in header [{}]", remoteUser, REMOTE_USER);
        }
        for (final String headerName : Collections.list(request.getHeaderNames())) {
            if (headerName.startsWith(ATTRIBUTE_PREFIX)) {
                final String headerValue = request.getHeader(headerName);
                LOGGER.debug(
                        "[SAML Shibboleth] Authn header [{}]<{}:{}>",
                        remoteUser,
                        headerName,
                        headerValue
                );
                osfPostgresCredential.getDelegationAttributes().put(
                        headerName.substring(ATTRIBUTE_PREFIX.length()),
                        headerValue
                );
            }
        }
        return osfPostgresCredential;
    }

    /**
     * Construct an {@link OsfPostgresCredential} object with username and verification key.
     *
     * @param username the username in request parameters
     * @param verificationKey the verification key in request parameters
     * @return an {@link OsfPostgresCredential} object
     */
    private OsfPostgresCredential constructCredentialsFromUsernameAndVerificationKey(final String username, final String verificationKey) {
        final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
        osfPostgresCredential.setUsername(username);
        osfPostgresCredential.setVerificationKey(verificationKey);
        osfPostgresCredential.setRememberMe(false);
        LOGGER.debug("User [{}] found in request w/ verificationKey", username);
        return osfPostgresCredential;
    }

    /**
     * Remove the shibboleth session cookie which is created by the Apache Shibboleth server after successful SAML authentication.
     *
     * @param context the Request Context
     */
    private void removeShibbolethSessionCookie(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            for (final Cookie cookie : cookies) {
                if (cookie.getName().startsWith(SHIBBOLETH_COOKIE_PREFIX)) {
                    final Cookie shibbolethCookie = new Cookie(cookie.getName(), null);
                    shibbolethCookie.setMaxAge(0);
                    response.addCookie(shibbolethCookie);
                }
            }
        }
    }

    /**
     * Extract delegated authentication data from the given {@link OsfPostgresCredential} object and normalize it as
     * required by the OSF API institution authentication endpoint.
     *
     * @param credential the credential object bearing delegated authentication data
     *
     * @return a {@link JSONObject} object bearing data required by the OSF API institution authentication endpoint
     * @throws ParserConfigurationException a parser configuration exception
     * @throws TransformerException a transformer exception
     */
    private JSONObject extractInstnAuthnDataFromCredential(final OsfPostgresCredential credential)
            throws ParserConfigurationException, TransformerException {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.newDocument();
        final Element rootElement = document.createElement("auth");
        document.appendChild(rootElement);

        final Element delegationProtocolAttr = document.createElement("attribute");
        delegationProtocolAttr.setAttribute("name", "delegation-protocol");
        delegationProtocolAttr.setAttribute("value", credential.getDelegationProtocol().getId());
        rootElement.appendChild(delegationProtocolAttr);

        for (final String key : credential.getDelegationAttributes().keySet()) {
            final Element attribute = document.createElement("attribute");
            attribute.setAttribute("name", key);
            attribute.setAttribute("value", credential.getDelegationAttributes().get(key));
            rootElement.appendChild(attribute);
        }

        final DOMSource source = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        instnAuthnRespTransformer.transform(source, result);

        return XML.toJSONObject(writer.getBuffer().toString());
    }

    /**
     * Securely notify OSF API of a successful institution authentication between OSF CAS and an external IdP. This
     * allows OSF API to either create a verified OSF account or find an existing active OSF account, and then assign
     * institutional affiliation to them. OSF API returns HTTP 204 if successful and HTTP 401 / 403 if failed. Refer
     * to the following code for the latest behavior of the institution authentication endpoint of OSF API:
     * https://github.com/CenterForOpenScience/osf.io/blob/develop/api/institutions/authentication.py
     *
     * @param credential the credential object bearing delegated authentication data
     * @return {@link OsfApiInstitutionAuthenticationResult} an object that stores institution and user info on success
     * @throws AccountException if there is an issue with authentication data or if the OSF API request has failed
     */
    private OsfApiInstitutionAuthenticationResult notifyOsfApiOfInstnAuthnSuccess(
            final RequestContext context,
            final OsfPostgresCredential credential
    ) throws AccountException {

        // Parse and normalize institution SSO authentication attributes
        final JSONObject normalizedPayload;
        try {
            normalizedPayload = extractInstnAuthnDataFromCredential(credential);
        } catch (final ParserConfigurationException | TransformerException e) {
            LOGGER.error("[CAS XSLT] Exception - Failed to normalize attributes in the credential: {}", e.getMessage());
            throw new InstitutionSsoAttributeParsingException("Attribute normalization failure");
        }
        // Verify required and optional attributes
        final JSONObject provider = normalizedPayload.optJSONObject("provider");
        if (provider == null) {
            LOGGER.error("[CAS XSLT] Error - Missing identity provider.");
            throw new InstitutionSsoAttributeMissingException("Missing identity provider");
        }
        final String institutionId = provider.optString("id").trim();
        if (institutionId.isEmpty()) {
            LOGGER.error("[CAS XSLT] Error - Empty identity provider");
            throw new InstitutionSsoAttributeMissingException("Empty identity provider");
        }
        final JSONObject user = provider.optJSONObject("user");
        if (user == null) {
            LOGGER.error("[CAS XSLT] Error - Missing institutional user");
            throw new InstitutionSsoAttributeMissingException("Missing institutional user");
        }
        // Note: SSO Identity didn't come from the normalized attribute set but came from a dedicated Shibboleth header. It was parsed,
        //       trimmed and stored in the credential object. Thus, it must be explicitly inserted into the payload.
        final String ssoIdentity = credential.getSsoIdentity();
        normalizedPayload.getJSONObject("provider").getJSONObject("user").put("ssoIdentity", credential.getSsoIdentity());
        // Note: For legacy reasons, the key for SSO email in the normalized attribute set is "username". SSO API endpoint now expects
        //       "ssoEmail". Thus, it also needs to be explicitly inserted into the payload with key "ssoEmail".
        final String ssoEmail = user.optString("username").trim();
        normalizedPayload.getJSONObject("provider").getJSONObject("user").put("ssoEmail", ssoEmail);
        final String ssoUser = String.format("institution=%s, ssoEmail=%s, ssoIdentity=%s", institutionId, ssoEmail, ssoIdentity);

        final String fullname = user.optString("fullname").trim();
        final String givenName = user.optString("givenName").trim();
        final String familyName = user.optString("familyName").trim();
        final String isMemberOf = user.optString("isMemberOf").trim();
        final String userRoles = user.optString("userRoles").trim();
        if (ssoEmail.isEmpty()) {
            LOGGER.error("[CAS XSLT] Error - Missing SSO Email for user: {}", ssoUser);
            throw new InstitutionSsoAttributeMissingException("Missing SSO Email)");
        }
        if (fullname.isEmpty() && (givenName.isEmpty() || familyName.isEmpty())) {
            LOGGER.error("[CAS XSLT] Error - Missing names: {}", ssoUser);
            throw new InstitutionSsoAttributeMissingException("Missing user's names");
        }
        if (!isMemberOf.isEmpty()) {
            LOGGER.info("[CAS XSLT] Shared SSO \"isMemberOf\" detected: {}, isMemberOf={}", ssoUser, isMemberOf);
        } else if (!userRoles.isEmpty()) {
            LOGGER.info("[CAS XSLT] Shared SSO \"userRoles\" detected: {}, userRoles={}", ssoUser, userRoles);
        } else {
            LOGGER.debug("[CAS XSLT] Shared SSO not eligible: {}", ssoUser);
        }
        // Parse the department attribute
        final String departmentRaw = user.optString("departmentRaw").trim();
        // Unlike all the above attributes that are released to us from the institutions, the `eduPerson` is a boolean
        // per-institution flag set by CAS in the "institutions-auth.xsl" file. It determines whether the department
        // attribute uses the eduPerson schema (https://wiki.refeds.org/display/STAN/eduPerson).
        final boolean eduPerson = user.optBoolean("eduPerson");
        String department = "";
        if (!departmentRaw.isEmpty()) {
            department = this.retrieveDepartment(departmentRaw, eduPerson, ssoUser);
            LOGGER.info(
                    "[CAS XSLT] Department detected and parsed: {}, eduPerson={}, departmentRaw={}, department={}",
                    ssoUser,
                    eduPerson,
                    departmentRaw,
                    department
            );
        } else {
            LOGGER.debug("[CAS XSLT] Department not provided: {}", ssoUser);
        }
        // Insert the `department` attribute into the payload, which does not overwrite `departmentRaw`.
        normalizedPayload.getJSONObject("provider").getJSONObject("user").put("department", department);
        // Check selective login and parse the filter
        final boolean isSelectiveSso = user.optBoolean("isSelectiveSso");
        String selectiveSsoFilter = "";
        if (!isSelectiveSso) {
            LOGGER.debug("[CAS XSLT] Selective SSO is not enabled: {}", ssoUser);
        } else {
            selectiveSsoFilter = user.optString("selectiveSsoFilter").trim();
            LOGGER.debug("[CAS XSLT] Selective SSO is enabled: {}, selectiveSsoFilter={}", ssoUser, selectiveSsoFilter);
        }
        // Insert the `selectiveSsoFilter` attribute into the payload
        normalizedPayload.getJSONObject("provider").getJSONObject("user").put("selectiveSsoFilter", selectiveSsoFilter);

        final String osfApiInstnAuthnPayload = normalizedPayload.toString();
        LOGGER.info("[CAS XSLT] All attributes checked: {}", ssoUser);
        LOGGER.debug("[CAS XSLT] All attributes checked: {}, normalizedPayload={}", ssoUser, osfApiInstnAuthnPayload);
        // Build the payload to be sent to OSF API institution authentication endpoint
        final String jweString;
        try {
            final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(ssoEmail)
                    .claim("data", osfApiInstnAuthnPayload)
                    .expirationTime(new Date(new Date().getTime() + SIXTY_SECONDS))
                    .build();
            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            final JWSSigner signer = new MACSigner(osfApiProperties.getInstnAuthnJwtSecret().getBytes());
            signedJWT.sign(signer);
            final JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT));
            jweObject.encrypt(new DirectEncrypter(osfApiProperties.getInstnAuthnJweSecret().getBytes()));
            jweString = jweObject.serialize();
        } catch (final JOSEException e) {
            LOGGER.error("[OSF API] Exception - Failed to construct API Payload: {}, error={}", ssoUser, e.getMessage());
            throw new InstitutionSsoOsfApiFailedException("OSF CAS failed to build JWT / JWE payload for OSF API");
        }
        // Send the POST request to OSF API to verify an existing institution user or to create a new one
        int statusCode = -1;
        int retry = 0;
        HttpResponse httpResponse = null;
        InstitutionSsoOsfApiFailedException casError = null;
        while (retry < OSF_API_RETRY_LIMIT) {
            retry += 1;
            // Reset exception from previous attempt
            casError = null;
            try {
                httpResponse = Request.Post(osfApiProperties.getInstnAuthnEndpoint())
                        .connectTimeout(SIXTY_SECONDS)
                        .socketTimeout(SIXTY_SECONDS)
                        .addHeader(new BasicHeader("Content-Type", "text/plain"))
                        .bodyString(jweString, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnResponse();
                statusCode = httpResponse.getStatusLine().getStatusCode();
                LOGGER.debug("[OSF API] Response Received: {}, attempt={}, status={}", ssoUser, retry, statusCode);
                // CAS expects OSF API to return HTTP 204 OK with no content if authentication succeeds
                if (statusCode == HttpStatus.SC_NO_CONTENT) {
                    LOGGER.info("[OSF API] Success - API request succeeded: {}, attempt={}, status={}", ssoUser, retry, statusCode);
                    return new OsfApiInstitutionAuthenticationResult(institutionId, ssoEmail, ssoIdentity);
                }
                if (OSF_API_RETRY_STATUS.contains(statusCode)) {
                    LOGGER.error("[OSF API] Failure - Server Error: {}, attempt={}, status={}", ssoUser, retry, statusCode);
                    casError = new InstitutionSsoOsfApiFailedException("Communication Error between OSF CAS and OSF API");
                } else {
                    break;
                }
            } catch (final IOException e) {
                LOGGER.error("[OSF API] Exception - IO Exception: {}, attempt={}, error={}", ssoUser, retry, e.getMessage());
                casError = new InstitutionSsoOsfApiFailedException("Communication Error between OSF CAS and OSF API");
            }
            try {
                TimeUnit.SECONDS.sleep(OSF_API_RETRY_DELAY_IN_SECONDS * retry);
            } catch (InterruptedException e) {
                LOGGER.error("[OSF API] Exception - Retry Interrupted: {}, attempt={}, error={}", ssoUser, retry, e.getMessage());
                casError = new InstitutionSsoOsfApiFailedException("Communication Error between OSF CAS and OSF API");
                break;
            }
        }
        if (casError != null) {
            throw casError;
        }
        // Handler unexpected exceptions (i.e. any status other than 403)
        if (statusCode != HttpStatus.SC_FORBIDDEN) {
            LOGGER.error("[OSF API] Failure - Unexpected HTTP response code: {}, statusCode={}", ssoUser, statusCode);
            throw new InstitutionSsoOsfApiFailedException("OSF API failed to process CAS request");
        }
        // CAS expects OSF API to return HTTP 403 FORBIDDEN with error details if authentication fails.
        String responseRaw;
        try {
            HttpEntity entity = httpResponse.getEntity();
            responseRaw = EntityUtils.toString(entity);
        } catch (final IOException | ParseException e) {
            LOGGER.error("[OSF API] Exception - Invalid Response Entity: {}, error={}", ssoUser, e.getMessage());
            throw new InstitutionSsoFailedException("CAS fails to parse OSF API error response");
        }
        try {
            // Attempt to identify and handle customized HTTP 403 FORBIDDEN failures
            final JsonObject responseJson = JsonParser.parseString(responseRaw).getAsJsonObject();
            final JsonArray errorList = responseJson.getAsJsonArray("errors");
            for (final JsonElement error : errorList) {
                if (!error.isJsonObject()) {
                    LOGGER.warn("[OSF API] Warning - Invalid JSON Response: error is not a JSON object, check next");
                    continue;
                }
                if (!((JsonObject) error).has("detail")) {
                    LOGGER.warn("[OSF API] Warning - Invalid Response: missing key \"detail\" in the error object, check next");
                    continue;
                }
                final String errorDetail = ((JsonObject) error).get("detail").getAsString();
                if (OsfApiPermissionDenied.INSTITUTION_SSO_SELECTIVE_LOGIN_DENIED.getId().equals(errorDetail)) {
                    LOGGER.error("[OSF API] Failure - Institution Selective SSO Not Allowed: {}, filter={}", ssoUser, selectiveSsoFilter);
                    setSsoErrorContext(
                            context,
                            InstitutionSsoSelectiveLoginDeniedException.class.getSimpleName(),
                            String.format("Institution Selective SSO Not Allowed: %s", ssoUser),
                            ssoEmail,
                            ssoIdentity,
                            institutionId,
                            OsfInstitutionUtils.getInstitutionSupportEmail(this.jpaOsfDao, institutionId)
                    );
                    throw new InstitutionSsoSelectiveLoginDeniedException("OSF API denies selective SSO login");
                }
                if (OsfApiPermissionDenied.INSTITUTION_SSO_DUPLICATE_IDENTITY.getId().equals(errorDetail)) {
                    LOGGER.error("[OSF API] Failure - Duplicate SSO Identity: {}", ssoUser);
                    throw new InstitutionSsoDuplicateIdentityException("OSF API can't handle duplicate SSO identity");
                }
                if (OsfApiPermissionDenied.INSTITUTION_SSO_ACCOUNT_INACTIVE.getId().equals(errorDetail)) {
                    LOGGER.error("[OSF API] Failure - Inactive Account: {}", ssoUser);
                    throw new InstitutionSsoAccountInactiveException("OSF API denies inactive account");
                }
            }
            // Handle unidentified HTTP 403 FORBIDDEN failures
            LOGGER.error("[OSF API] Failure - HTTP 403 FORBIDDEN: {}, statusCode={}", ssoUser, statusCode);
            throw new InstitutionSsoOsfApiFailedException("OSF API failed to process CAS request");
        } catch (final JsonParseException | IllegalStateException e) {
            LOGGER.error("[OSF API] Exception - Invalid Response: {}, error={}", ssoUser, e.getMessage());
            throw new InstitutionSsoOsfApiFailedException("CAS failed to parse OSF API error response");
        }
    }

    /**
     * Retrieve the department value from the raw department string.
     *
     * @param departmentRaw the raw department string
     * @param eduPerson whether the department attribute uses eduPerson schema
     * @param ssoUser a string that includes institution ID, SSO email and SSO identity of the current SSO user
     * @return the department value
     */
    private String retrieveDepartment(final String departmentRaw, final boolean eduPerson, final String ssoUser) {

        // Return the raw value as it is if institutions do not use eduPerson schema for the department attribute
        if (!eduPerson) {
            return departmentRaw;
        }

        // For institutions that use the eduPerson schema, the department must be retrieved from the raw value. Here is
        // an example: "ou=Music Department, o=Notre Dame, dc=nd, dc=edu", whose syntax is LDAP Distinguished Names.
        try {
            final LdapName dn = new LdapName(departmentRaw);
            for (int i = dn.size() - 1; i >=0; i--) {
                final String rdn = dn.get(i);
                if (rdn.startsWith(LDAP_DN_OU_PREFIX)) {
                    return rdn.substring(LDAP_DN_OU_PREFIX.length());
                }
            }
        } catch (final InvalidNameException | IndexOutOfBoundsException e) {
            // Return an empty string if the syntax is wrong
            LOGGER.error("[CAS XSLT] Exception - Invalid LDAP DN: {}, departmentRaw={}, error={}", ssoUser, departmentRaw, e.getMessage());
            return "";
        }
        return "";
    }

    /**
     * Prepare {@link OsfCasSsoErrorContext} and put it in flow.
     *
     * @param context the request context
     * @param handleErrorName the error name
     * @param errorMessage the error message
     * @param ssoEmail user's SSO email
     * @param ssoIdentity user's SSO identity
     * @param institutionId institution ID
     * @param institutionSupportEmail institution support email
     */
    private void setSsoErrorContext(
            final RequestContext context,
            final String handleErrorName,
            final String errorMessage,
            final String ssoEmail,
            final String ssoIdentity,
            final String institutionId,
            final String institutionSupportEmail
    ) {
        OsfCasSsoErrorContext ssoErrorContext =  new OsfCasSsoErrorContext(
                handleErrorName,
                errorMessage,
                ssoEmail,
                ssoIdentity,
                institutionId,
                institutionSupportEmail
        );
        context.getFlowScope().put(PARAMETER_SSO_ERROR_CONTEXT, ssoErrorContext);
    }
}
