package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import io.cos.cas.osf.authentication.credential.OsfPostgresCredential;
import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.StringUtils;

import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Terminates the CAS SSO session by destroying all SSO state data (i.e. TGT, cookies).
 *
 * This action has been updated to support customized logout for institution login. If the current authenticated session was
 * created from an institution SSO and if that institution has set up customized logout URL, this action returns an event (after having
 * securely and fully clears the session) that will lead to the flow being redirected back to the institution's logout endpoint. Otherwise,
 * it follows the vanilla behavior as implemented in <a href="https://github.com/apereo/cas/blob/6.2.x/support/cas-server-support-actions/src/main/java/org/apereo/cas/web/flow/logout/TerminateSessionAction.java">TerminateSessionAction (6.2.x)</a>.
 *
 * @author Marvin S. Addison
 * @author Longze Chen
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TerminateSessionAction extends AbstractAction {

    /**
     * Parameter to indicate the customized institution logout URL.
     */
    public final static String PARAMETER_INSTITUTION_LOGOUT_ENDPOINT = "institutionLogoutRedirectUrl";

    /**
     * Parameter to indicate logout request is confirmed.
     */
    public static final String REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED = "LogoutRequestConfirmed";

    /**
     * The event factory.
     */
    protected final EventFactorySupport eventFactorySupport = new EventFactorySupport();

    /**
     * The authentication service.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * The TGT cookie generator.
     */
    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    /**
     * The warn cookie generator.
     */
    protected final CasCookieBuilder warnCookieGenerator;

    /**
     * The logout properties.
     */
    protected final LogoutProperties logoutProperties;

    /**
     * The JPA DAO to access OSF models.
     */
    private final JpaOsfDao jpaOsfDao;

    /**
     * Check if the logout must be confirmed.
     *
     * @param requestContext the request context
     * @return if the logout must be confirmed
     */
    protected static boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameterMap().containsKey(REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val terminateSession = FunctionUtils.doIf(logoutProperties.isConfirmLogout(),
                () -> isLogoutRequestConfirmed(requestContext),
                () -> Boolean.TRUE)
                .get();

        if (terminateSession) {
            return terminate(requestContext);
        }
        return this.eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_WARN);
    }

    /**
     * Terminates the CAS SSO session by destroying the TGT (if any) and removing cookies related to the SSO session.
     *
     * @param context Request context.
     * @return "success"
     */
    @SneakyThrows
    public Event terminate(final RequestContext context) {

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        val tgtId = getTicketGrantingTicket(context);
        val institutionId = getInstitutionIdFromTicketGrantingTicket(tgtId);
        if (StringUtils.isNotBlank(tgtId)) {
            LOGGER.trace("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId);
            val logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
            WebUtils.putLogoutRequests(context, logoutRequests);
        }
        LOGGER.trace("Removing CAS cookies");
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        destroyApplicationSession(request, response);
        LOGGER.debug("Terminated all CAS sessions successfully.");

        // Option 1: redirect to institution logout if 1) the session is created via institution SSO and 2) the institution has a logout URL
        if (StringUtils.isNotBlank(institutionId)) {
            val institution = jpaOsfDao.findOneInstitutionById(institutionId);
            if (institution != null) {
                val institutionLogoutUrl = institution.getLogoutUrl();
                if (StringUtils.isNotBlank(institutionLogoutUrl)) {
                    LOGGER.debug("Institution [{}] has a customized logout URL [{}]", institutionId, institutionLogoutUrl);
                    context.getFlowScope().put(PARAMETER_INSTITUTION_LOGOUT_ENDPOINT, institutionLogoutUrl);
                    return this.eventFactorySupport.event(this, OsfCasWebflowConstants.TRANSITION_ID_INSTITUTION_LOGOUT);
                }
                LOGGER.debug("Institution [{}] does not have a customized logout URL", institutionId);
            }
            LOGGER.warn("Invalid institution [{}] from ticket granting ticket.", institutionId);
        }

        if (StringUtils.isNotBlank(logoutProperties.getRedirectUrl())) {
            WebUtils.putLogoutRedirectUrl(context, logoutProperties.getRedirectUrl());
            return this.eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_REDIRECT);
        }

        return this.eventFactorySupport.success(this);
    }

    // Retrieve the ticket granting ticket ID from the request context.
    private String getTicketGrantingTicket(final RequestContext context) {
        val tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            return this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        return tgtId;
    }

    /**
     * Destroy application session.
     * Also kills all delegated authn profiles via pac4j.
     *
     * @param request  the request
     * @param response the response
     */
    @SuppressWarnings("java:S2441")
    protected void destroyApplicationSession(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.trace("Destroying application session");
        val context = new JEEContext(request, response, new JEESessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());
        manager.logout();

        val session = request.getSession(false);
        if (session != null) {
            val requestedUrl = session.getAttribute(Pac4jConstants.REQUESTED_URL);
            session.invalidate();
            if (requestedUrl != null && !requestedUrl.equals(StringUtils.EMPTY)) {
                request.getSession(true).setAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
            }
        }
    }

    // Retrieve institution ID from the current session. It only returns the institution ID if it is found and if the the session is
    // created via institution SSO. Otherwise, return null.
    private String getInstitutionIdFromTicketGrantingTicket(final String ticketId) {
        TicketGrantingTicket ticketGrantingTicket = null;
        if (StringUtils.isNotBlank(ticketId)) {
            try {
                ticketGrantingTicket = centralAuthenticationService.getTicket(ticketId, TicketGrantingTicket.class);
            } catch (final Exception e) {
                LOGGER.warn("Invalid ticket granting ticket ID");
            }
        }
        if (ticketGrantingTicket != null) {
            final Authentication authentication = ticketGrantingTicket.getAuthentication();
            val parameterRemotePrincipal =OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_REMOTE_PRINCIPAL;
            val parameterInstitutionId = OsfPostgresCredential.AUTHENTICATION_ATTRIBUTE_INSTITUTION_ID;
            if (authentication != null) {
                if (authentication.getAttributes().containsKey(parameterRemotePrincipal)) {
                    final boolean remotePrincipal = (Boolean) authentication.getAttributes().get(parameterRemotePrincipal).get(0);
                    if (remotePrincipal && authentication.getAttributes().containsKey(parameterInstitutionId)) {
                        return (String) authentication.getAttributes().get(parameterInstitutionId).get(0);
                    }
                }
            }
        }
        return null;
    }
}
