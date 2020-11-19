package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.authentication.exception.InstitutionSsoFailedException;
import io.cos.cas.osf.authentication.support.DelegationProtocol;
import io.cos.cas.osf.configuration.model.OsfApiProperties;
import io.cos.cas.osf.configuration.model.OsfUrlProperties;
import io.cos.cas.osf.web.support.OsfApiInstitutionAuthenticationResult;

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
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import org.json.JSONObject;
import org.json.XML;

import org.springframework.util.ResourceUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.login.AccountException;
import javax.servlet.http.HttpServletRequest;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link OsfPrincipalFromNonInteractiveCredentialsAction}.
 *
 * Extends {@link AbstractNonInteractiveCredentialsAction} to check if there is any non-interactive authentication
 * available. In the case of authentication delegation, if credential with matching client found, simply use that
 * credential and return the success event (i.e. to authenticate and create the ticket granting ticket). In the case
 * of "username / verification key" login, if both found in requests parameters, create the OSF credential and return
 * success. Otherwise, return the error event and go to the login page for default "username / password" login.
 *
 * @author Longze Chen
 * @since 20.0.0
 */
@Slf4j
@Setter
@Getter
public class OsfPrincipalFromNonInteractiveCredentialsAction extends AbstractNonInteractiveCredentialsAction {

    private static final String USERNAME_PARAMETER_NAME = "username";

    private static final String VERIFICATION_KEY_PARAMETER_NAME = "verification_key";

    private static final String OSF_URL_FLOW_PARAMETER = "osfUrl";

    private static final String AUTHENTICATION_EXCEPTION = "authnError";

    private static final int SIXTY_SECONDS = 60 * 1000;

    public static final String INSTITUTION_CLIENTS_PARAMETER_NAME = "institutionClients";

    public static final String NON_INSTITUTION_CLIENTS_PARAMETER_NAME = "nonInstitutionClients";

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

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
        this.osfUrlProperties = osfUrlProperties;
        this.osfApiProperties = osfApiProperties;
        this.authnDelegationClients = authnDelegationClients;
    }

    @SneakyThrows
    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {

        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        // Check if credential already exists from delegated authentication
        final Credential credential = WebUtils.getCredential(context);
        if (credential != null) {
            LOGGER.debug("Existing credential found in context of type [{}]", credential.getClass());
            if (credential instanceof ClientCredential) {
                final String clientName = ((ClientCredential) credential).getClientName();
                if (authnDelegationClients.get(NON_INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid non-institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    return credential;
                }
                if (authnDelegationClients.get(INSTITUTION_CLIENTS_PARAMETER_NAME).contains(clientName)) {
                    LOGGER.debug(
                            "Valid institution authn delegation client [{}] found with principal [{}]",
                            clientName,
                            credential.getId()
                    );
                    final Authentication authentication = WebUtils.getAuthentication(context);
                    final Principal principal = authentication.getPrincipal();
                    final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
                    osfPostgresCredential.setRemotePrincipal(Boolean.TRUE);
                    osfPostgresCredential.setDelegationProtocol(DelegationProtocol.CAS_PAC4J);
                    osfPostgresCredential.getDelegationAttributes().put("Cas-Identity-Provider", clientName);
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
                    } else {
                        LOGGER.error("[CAS PAC4J] No attributes for user '{} with client '{}'", principal.getId(), clientName);
                    }

                    final OsfApiInstitutionAuthenticationResult remoteUserInfo = notifyOsfApiOfInstnAuthnSuccess(osfPostgresCredential);
                    osfPostgresCredential.setUsername(remoteUserInfo.getUsername());
                    osfPostgresCredential.setInstitutionId(remoteUserInfo.getInstitutionId());
                    WebUtils.removeCredential(context);
                    return osfPostgresCredential;
                }
                LOGGER.debug("Unsupported delegation client [{}]", clientName);
                return null;
            }
            LOGGER.debug("Unsupported delegation credential [{}]", credential.getClass().getSimpleName());
            return null;
        }

        LOGGER.debug("No valid credential found in the request context.");
        final OsfPostgresCredential osfPostgresCredential = new OsfPostgresCredential();
        final String username = request.getParameter(USERNAME_PARAMETER_NAME);
        final String verificationKey = request.getParameter(VERIFICATION_KEY_PARAMETER_NAME);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(verificationKey)) {
            osfPostgresCredential.setUsername(username);
            osfPostgresCredential.setVerificationKey(verificationKey);
            osfPostgresCredential.setRememberMe(true);
            LOGGER.debug("User [{}] found in request w/ verificationKey", username);
            return osfPostgresCredential;
        }
        LOGGER.debug("No username or verification key found in the request parameters.");
        return null;
    }

    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        OsfUrlProperties osfUrl = Optional.of(context).map(requestContext
                -> (OsfUrlProperties) requestContext.getFlowScope().get(OSF_URL_FLOW_PARAMETER)).orElse(null);
        if (osfUrl == null) {
            context.getFlowScope().put(OSF_URL_FLOW_PARAMETER, osfUrlProperties);
        }
        return super.doPreExecute(context);
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
     * Extract delegated authentication data from the given {@link OsfPostgresCredential} object and normalize it as
     * required by the OSF API institution authentication endpoint.
     *
     * @param credential the credential object bearing delegated authentication data
     *
     * @return a {@link JSONObject} object bearing data required by the OSF API institution authentication endpoint
     * @throws ParserConfigurationException a parser configuration exception
     * @throws TransformerException a transformer exception
     */
    protected JSONObject extractInstnAuthnDataFromCredential(final OsfPostgresCredential credential)
            throws ParserConfigurationException, TransformerException {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.newDocument();
        final Element rootElement = document.createElement("auth");
        document.appendChild(rootElement);

        final Element delegationProtocolAttr = document.createElement("attribute");
        delegationProtocolAttr.setAttribute("name", "Delegation-Protocol");
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
    protected OsfApiInstitutionAuthenticationResult notifyOsfApiOfInstnAuthnSuccess(
            final OsfPostgresCredential credential
    ) throws AccountException {

        final JSONObject normalizedPayload;
        try {
            normalizedPayload = extractInstnAuthnDataFromCredential(credential);
        } catch (final ParserConfigurationException | TransformerException e) {
            LOGGER.error("[CAS XSLT] Failed to normalize attributes in the credential: {}", e.getMessage());
            throw new InstitutionSsoFailedException("Attribute normalization failure");
        }

        final JSONObject provider = normalizedPayload.optJSONObject("provider");
        if (provider == null) {
            LOGGER.error("[CAS XSLT] Missing identity provider.");
            throw new InstitutionSsoFailedException("Missing identity provider");
        }
        final String institutionId = provider.optString("id").trim();
        if (institutionId.isEmpty()) {
            LOGGER.error("[CAS XSLT] Empty identity provider");
            throw new InstitutionSsoFailedException("Empty identity provider");
        }
        final JSONObject user = provider.optJSONObject("user");
        if (user == null) {
            LOGGER.error("[CAS XSLT] Missing institutional user");
            throw new InstitutionSsoFailedException("Missing institutional user");
        }
        final String username = user.optString("username").trim();
        final String fullname = user.optString("fullname").trim();
        final String givenName = user.optString("givenName").trim();
        final String familyName = user.optString("familyName").trim();
        final String isMemberOf = user.optString("isMemberOf").trim();
        if (username.isEmpty()) {
            LOGGER.error("[CAS XSLT] Missing email (username) for user at institution '{}'", institutionId);
            throw new InstitutionSsoFailedException("Missing email (username)");
        }
        if (fullname.isEmpty() && (givenName.isEmpty() || familyName.isEmpty())) {
            LOGGER.error("[CAS XSLT] Missing names: username={}, institution={}", username, institutionId);
            throw new InstitutionSsoFailedException("Missing user's names");
        }
        if (!isMemberOf.isEmpty()) {
            LOGGER.info(
                    "[CAS XSLT] Secondary institution detected. SSO is '{}' and member is '{}'",
                    institutionId,
                    isMemberOf
            );
        }
        final String payload = normalizedPayload.toString();
        LOGGER.info(
                "[CAS XSLT] All attributes checked: username={}, institution={}, member={}",
                username,
                institutionId,
                isMemberOf
        );
        LOGGER.debug(
                "[CAS XSLT] All attributes checked: username={}, institution={}, member={}, normalizedPayload={}",
                username,
                institutionId,
                isMemberOf,
                payload
        );

        final String jweString;
        try {
            final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("data", payload)
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
            LOGGER.error(
                    "[OSF API] Notify Remote Principal Authenticated Failed: Payload Error - {}",
                    e.getMessage()
            );
            throw new InstitutionSsoFailedException("OSF CAS failed to build JWT / JWE payload for OSF API");
        }

        try {
            final HttpResponse httpResponse = Request.Post(osfApiProperties.getInstnAuthnEndpoint())
                    .addHeader(new BasicHeader("Content-Type", "text/plain"))
                    .bodyString(jweString, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            LOGGER.info(
                    "[OSF API] Notify Remote Principal Authenticated Response: username={} statusCode={}",
                    username,
                    statusCode
            );
            if (statusCode != HttpStatus.SC_NO_CONTENT) {
                final String responseString = new BasicResponseHandler().handleResponse(httpResponse);
                LOGGER.error(
                        "[OSF API] Notify Remote Principal Authenticated Failed: statusCode={}, body={}",
                        statusCode,
                        responseString
                );
                throw new InstitutionSsoFailedException("OSF API failed to process CAS request");
            }
            return new OsfApiInstitutionAuthenticationResult(username, institutionId);
        } catch (final IOException e) {
            LOGGER.error(
                    "[OSF API] Notify Remote Principal Authenticated Failed: Communication Error - {}",
                    e.getMessage()
            );
            throw new InstitutionSsoFailedException("Communication Error between OSF CAS and OSF API");
        }
    }
}
