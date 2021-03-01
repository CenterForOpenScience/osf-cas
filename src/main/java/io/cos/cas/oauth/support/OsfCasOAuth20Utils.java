package io.cos.cas.oauth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.cos.cas.osf.configuration.model.OsfUrlProperties;
import io.cos.cas.osf.web.flow.support.OsfCasWebflowConstants;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class adds customized features to support OAuth 2.0 for OSF CAS that are not provided by its vanilla Apereo CAS counterpart.
 *
 * See {@link org.apereo.cas.support.oauth.util.OAuth20Utils}.
 *
 * @author Longze Chen
 * @since 21.x.0
 */
@Slf4j
@UtilityClass
public class OsfCasOAuth20Utils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);

    /**
     * Produce Oauth 2.0 error view.
     *
     * @param errorCode the error code / short name
     * @param errorMsg the error message / long name
     * @param errorParam the param name that caused the error
     * @param scopeSet the scope set
     * @param osfUrl the OSF URLs
     * @return ModelAndView
     */
    public static ModelAndView produceOAuth20ErrorView(
            final String errorCode,
            final String errorMsg,
            final String errorParam,
            final Set<String> scopeSet,
            final OsfUrlProperties osfUrl) {
        val modelContext = new OsfCasOAuth20ModelContext(errorCode, errorMsg, errorParam, scopeSet, osfUrl);
        return new ModelAndView(OsfCasWebflowConstants.VIEW_ID_OAUTH_20_ERROR_VIEW, modelContext.getModelContextMap());
    }

    /**
     * Produce Oauth 2.0 error view.
     *
     * @param modelContext the OSF CAS OAuth 2.0 model and view context
     * @return ModelAndView
     */
    public static ModelAndView produceOAuth20ErrorView(final OsfCasOAuth20ModelContext modelContext) {
        return new ModelAndView(OsfCasWebflowConstants.VIEW_ID_OAUTH_20_ERROR_VIEW, modelContext.getModelContextMap());
    }

    /**
     * Write to the output this error with customized status.
     *
     * @param response the response
     * @param error    the error message
     * @param status   an {@link HttpStatus} object
     * @return json-backed view.
     */
    public static ModelAndView writeError(final HttpServletResponse response, final String error, final HttpStatus status) {
        val model = new HashMap<String, Object>();
        model.put(OAuth20Constants.ERROR, error);
        val mv = new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
        mv.setStatus(status);
        response.setStatus(status.value());
        return mv;
    }

    /**
     * Retrieve all tokens that belongs to a given client from the ticket registry.
     *
     * @param clientId the client ID
     * @return a list of tokens
     */
    public List<OAuth20Token> getOAuth20ClientTokens(final TicketRegistry ticketRegistry, final String clientId) {
        return ticketRegistry == null ? null : ticketRegistry.getOAuth20ClientTokens(clientId);
    }

    public Map<String, List<? extends OAuth20Token>> getOAuth20ClientAccessAndRefreshTokens(final TicketRegistry ticketRegistry, final String clientId) {
        if (ticketRegistry == null) {
            LOGGER.error("ticket registry is null");
            return null;
        }
        val tokenMap = new HashMap<String, List<? extends OAuth20Token>>();
        val accessTokens = new ArrayList<OAuth20AccessToken>();
        val refreshTokens = new ArrayList<OAuth20RefreshToken>();
        val tokens = getOAuth20ClientTokens(ticketRegistry, clientId);
        for (val token: tokens) {
            if (token instanceof OAuth20RefreshToken) {
                refreshTokens.add((OAuth20RefreshToken) token);
            } else if (token instanceof OAuth20AccessToken) {
                accessTokens.add((OAuth20AccessToken) token);
            } else {
                LOGGER.warn("token [id={}] of unexpected type [class={}] detected", token.getId(), token.getClass());
            }
        }
        tokenMap.put(OAuth20AccessToken.PREFIX, accessTokens);
        tokenMap.put(OAuth20RefreshToken.PREFIX, refreshTokens);
        return tokenMap;
    }
}
