package org.b3log.symphony.processor.advice;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Token check for app request, and check token come from app, will be stop login if token is invalid.
 *
 * @author changming.yang.ah@gmail.com
 * @version 1.2.0.2, Jun 28, 2015
 * @since 0.2.5
 */
@Named
@Singleton
public class TokenCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TokenCheck.class.getName());

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        //todo check token for current user

        try {
            JSONObject currentUser = userQueryService.getCurrentUser(request);
            if (null == currentUser && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                throw new RequestProcessAdviceException(exception);
            }

            currentUser = userQueryService.getCurrentUser(request);
            final int point = currentUser.optInt(UserExt.USER_POINT);
            final int appRole = currentUser.optInt(UserExt.USER_APP_ROLE);
            if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
                currentUser.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                currentUser.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
            }

            request.setAttribute(User.USER, currentUser);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Login check failed");
            throw new RequestProcessAdviceException(exception);
        }
    }
}