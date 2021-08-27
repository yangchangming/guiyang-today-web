/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.api;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.cache.TokenCache;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.AuthUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Login by app processor.
 *
 * @author <a href="http://wdx.me">DX</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jun 23, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class LoginProcessor {
    private static Logger logger = Logger.getLogger(LoginProcessor.class);

    @Inject
    private UserQueryService userQueryService;

    @Inject
    private UserMgmtService userMgmtService;

    @Inject
    private TokenCache tokenCache;

    /**
     * Login by wechat, register new user if user not exist.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     * @throws JSONException JSONException
     */
    @RequestProcessing(value = "/api/v1/login/wechat", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void login4Wechat(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        final String error = "invalid grant";
        final String errorDescription = "The provided authorization grant is invalid, expired, revoked, does not match";
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);
        JSONObject result = new JSONObject();
        renderer.setJSONObject(result);

        result = Requests.parseRequestJSONObject(request, response);
        String unionId = result.getString("unionId");
        String oid = null;
        try {
            JSONObject user = userQueryService.getUserByUnionId(unionId);

            //todo 看看微信返回什么数据
            if (user==null) {
                user = new JSONObject();
                user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);
                user.put(UserExt.USER_B3_KEY, unionId);
                user.put(UserExt.USER_QQ, result.getString("sex"));
                user.put(User.USER_PASSWORD, "");
                user.put(UserExt.USER_NICKNAME, result.getString("nickName"));
                user.put(User.USER_NAME, result.getString("nickName"));
                user.put(UserExt.USER_PROVINCE, result.getString("province"));
                user.put(UserExt.USER_CITY, result.getString("city"));
                user.put(UserExt.USER_COUNTRY, result.getString("country"));
                user.put(UserExt.USER_AVATAR_URL, result.getString("headImgUrl"));
                oid = userMgmtService.addUser(user);
            }

//            if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)
//                    || UserExt.USER_STATUS_C_INVALID_LOGIN == user.optInt(UserExt.USER_STATUS)) {
//                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
//                ret.put("error", error);
//                ret.put("error_description", errorDescription);
//                return;
//            }
            if (oid==null){
                oid = user.getString("oid");
            }
            String token = null;
            token = AuthUtil.buildToken(unionId); //unionID 进行hash
            if (token==null || "".equals(token)){
                result.put("error", error);
                result.put("error_description", "Token build failure.");
                return;
            }
            //放入本地缓存
            tokenCache.put(TokenCache.SESSION_PREFIX + oid, token);

            final String ip = Requests.getRemoteAddr(request);
            userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);
            result.put("access_token", token);
            result.put("token_type", "app");
            result.put("scope", "user");
            result.put("created_at", new Date().getTime());
            return;
        } catch (final ServiceException e) {
            result.put("error", error);
            result.put("error_description", errorDescription);
        }
    }
}
