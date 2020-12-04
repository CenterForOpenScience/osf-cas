package io.cos.cas.osf.web.flow.login;

import io.cos.cas.osf.configuration.model.OsfUrlProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * This is {@link OsfCasPreInitialFlowSetupAction}.
 *
 * @author Longze Chen
 * @since 20.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class OsfCasPreInitialFlowSetupAction extends AbstractAction {

    private static final String OSF_URL_FLOW_PARAMETER = "osfUrl";

    @NotNull
    private final OsfUrlProperties osfUrlProperties;

    @Override
    protected Event doExecute(final RequestContext context) {
        OsfUrlProperties osfUrl = Optional.of(context).map(requestContext
                -> (OsfUrlProperties) requestContext.getFlowScope().get(OSF_URL_FLOW_PARAMETER)).orElse(null);
        if (osfUrl == null) {
            context.getFlowScope().put(OSF_URL_FLOW_PARAMETER, osfUrlProperties);
        }
        return success();
    }
}
