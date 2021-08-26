package org.b3log.symphony.processor.advice;

import com.sun.tools.javac.comp.Check;
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
import java.io.OutputStream;
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

    private static final Logger LOGGER = Logger.getLogger(TokenCheck.class.getName());

    @Inject
    private UserQueryService userQueryService;

    @Inject
    private UserMgmtService userMgmtService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        //todo check token for current user

        String token = request.getHeader("token");
        String userId = request.getHeader("sign");

        try {
            if (token==null || userId==null || "".equals(token) || "".equals(userId)){
                String content = new ParamMissingResponse(null).getResponseBodyAsString();

                OutputStream outputStream = response.getOutputStream();
                response.setHeader("Content-Type", "application/json;charset=UTF-8");

                outputStream.write(content.getBytes("UTF-8"));
                outputStream.flush();
                return false;
            }

            if (userId!=null && !"".equals(userId)){
                User user = new User();
                user.setId(userId);
//                return SessionUtil.checkIn(user, token) && AuthUtil.checkIn(token, userId);

                boolean result = SessionUtil.checkIn(user, token);
                if (!result){
                    String content = new InvalidTokenResponse(null).getResponseBodyAsString();
                    OutputStream outputStream = response.getOutputStream();
                    response.setHeader("Content-Type", "application/json;charset=UTF-8");
                    outputStream.write(content.getBytes("UTF-8"));
                    outputStream.flush();
                }
                return result;
            }







            request.setAttribute(User.USER, currentUser);

        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Login check failed");
            throw new RequestProcessAdviceException(exception);
        }
    }
}