package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.authentication.support.OsfInstitutionUtils;
import io.cos.cas.osf.dao.JpaOsfDao;
import io.cos.cas.osf.web.support.OsfCasLoginContext;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OsfInstitutionLoginPreparationAction}.
 *
 * Extends {@link OsfAbstractLoginPreparationAction} to prepare the institution login page.
 *
 * @author Longze Chen
 * @since 20.1.0
 */
@Slf4j
public class OsfInstitutionLoginPreparationAction extends OsfAbstractLoginPreparationAction {

    @NotNull
    private final JpaOsfDao jpaOsfDao;

    @NotNull
    private final List<String> pac4jInstnClients;

    public OsfInstitutionLoginPreparationAction(
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
            final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
            final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
            final JpaOsfDao jpaOsfDao,
            final List<String> pac4jInstnClients
    ) {
        super(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy
        );
        this.jpaOsfDao = jpaOsfDao;
        this.pac4jInstnClients = pac4jInstnClients;
    }

    @Override
    protected Event doExecute(RequestContext context) {

        String target;
        String service = context.getRequestParameters().get("service");
        if (service != null) {
            service = URLEncoder.encode(service, StandardCharsets.UTF_8);
            target = URLEncoder.encode(String.format("/login?service=%s", service), StandardCharsets.UTF_8);
        } else {
            target = URLEncoder.encode("/login", StandardCharsets.UTF_8);
        }

        String institutionId = null;
        OsfCasLoginContext loginContext;
        loginContext = Optional.of(context).map(requestContext
                -> (OsfCasLoginContext) requestContext.getFlowScope().get(PARAMETER_LOGIN_CONTEXT)).orElse(null);
        if (loginContext != null) {
            institutionId = loginContext.getInstitutionId();
            if (!OsfInstitutionUtils.validateInstitutionForLogin(jpaOsfDao, institutionId)) {
                loginContext.setInstitutionId(null);
                context.getFlowScope().put(PARAMETER_LOGIN_CONTEXT, loginContext);
                institutionId = null;
            }
        }

        final Map<String, String> institutionLoginUrlMap
                = OsfInstitutionUtils.getInstitutionLoginUrlMap(jpaOsfDao, target, institutionId);
        final Map<String, String> institutionLoginUrlMapSorted;
        if (institutionId != null) {
            institutionLoginUrlMapSorted = institutionLoginUrlMap;
        } else {
            institutionLoginUrlMap.put("", " -- select an institution -- ");
            institutionLoginUrlMapSorted = OsfInstitutionUtils.sortByValue(institutionLoginUrlMap);
        }
        context.getFlowScope().put("institutions", institutionLoginUrlMapSorted);
        putPac4jInstnLoginUrls(context);
        return success();
    }

    private void putPac4jInstnLoginUrls(final RequestContext context) {
        final Map<String, String> pac4jInstitutionLoginUrlMap = new HashMap<>();
        final Set<? extends Serializable> clients = WebUtils.getDelegatedAuthenticationProviderConfigurations(context);
        for (final Serializable client: clients) {
            if (client instanceof DelegatedClientIdentityProviderConfiguration) {
                final String clientType = ((DelegatedClientIdentityProviderConfiguration) client).getType();
                if (PARAMETER_CAS_CLIENT_TYPE.equals(clientType)) {
                    final String clientName = ((DelegatedClientIdentityProviderConfiguration) client).getName();
                    if (!pac4jInstnClients.contains(clientName)) {
                        LOGGER.error("Invalid PAC4J institution clients: [{}]", clientName);
                    } else {
                        final String clientUrl = ((DelegatedClientIdentityProviderConfiguration) client).getRedirectUrl();
                        pac4jInstitutionLoginUrlMap.put(clientName, clientUrl);
                        LOGGER.warn("{}: {}", clientName, clientUrl);
                    }
                }
            }
        }
        context.getFlowScope().put("pac4jInstnLoginUrls", pac4jInstitutionLoginUrlMap);
    }
}
