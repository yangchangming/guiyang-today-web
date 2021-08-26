package org.b3log.symphony.api;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.TokenCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ArticleProcessor2
 * article api about everything
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2016-09-24
 */
@RequestProcessor
public class ArticleProcessor2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor2.class.getName());

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

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Client management service.
     */
    @Inject
    private ClientMgmtService clientMgmtService;

    /**
     * Client query service.
     */
    @Inject
    private ClientQueryService clientQueryService;


    /**
     * Add article by http post
     *
     * <p>
     * The request json object, for example,
     * <pre>
     * {
     *     "article": {
     *         "articleContent": "test content",
     *         "articlePermalink": "/#",
     *         "articleTags": "test",
     *         "articleTitle": "test"
     *     },
     *     "clientAdminEmail": "neocode@126.com",
     *     "clientHost": "http://www.gzit.info",
     *     "clientName": "第三方应用",
     *     "clientRuntimeEnv": "NIGHT",
     *     "clientVersion": "0.1",
     * }
     * </pre>
     * </p>
     *
     *
     * @param context
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestProcessing(value = "/api/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, TokenCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context,final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        context.renderJSON();
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        //唯一身份认证
        final String clientAdminEmail = requestJSONObject.getString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.getString(Client.CLIENT_NAME);
        final String clientVersion = requestJSONObject.getString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.getString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.getString(Client.CLIENT_RUNTIME_ENV);

        //validate beginning
        final JSONObject user = userQueryService.getUserByEmail(clientAdminEmail);
        if (null == user) {
            LOGGER.log(Level.WARN, "The user[email={0}, host={1}] not found in community", clientAdminEmail, clientHost);
            return;
        }

        final String userName = user.optString(User.USER_NAME);

        //user checking
        if (UserExt.USER_STATUS_C_VALID != user.optInt(UserExt.USER_STATUS)) {
            LOGGER.log(Level.WARN, "The user[name={0}, email={1}, host={2}] has been forbidden", userName, clientAdminEmail, clientHost);
            return;
        }


        //deal with article beginning

        final JSONObject originalArticle = requestJSONObject.getJSONObject(Article.ARTICLE);
        final String authorId = user.optString(Keys.OBJECT_ID);
        final String clientArticleId = originalArticle.optString(Keys.OBJECT_ID);

        final String articleTitle = originalArticle.optString(Article.ARTICLE_TITLE);
        String articleTags = Tag.formatTags(originalArticle.optString(Article.ARTICLE_TAGS));
        String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
        article.put(Article.ARTICLE_AUTHOR_ID, authorId);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, clientAdminEmail.toLowerCase().trim());

        final String permalink = originalArticle.optString(Article.ARTICLE_PERMALINK);

        final JSONObject articleExisted = articleQueryService.getArticleByClientArticleId(authorId, clientArticleId);
//        final boolean toAdd = null == articleExisted;

        // is add article forever
        final boolean toAdd = true;

        // article already exist in server
        if (!toAdd) {
            article.put(Keys.OBJECT_ID, articleExisted.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

        } else {
            final boolean isBroadcast = "aBroadcast".equals(permalink);
            if (isBroadcast) {
                articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该广播来自</span> "
                        + "<i style='margin-right:5px;'><a target='_blank' href='"
                        + clientHost + "'>" + clientName + "</a></i></span></p>";
            } else {
                articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
                        + "<i style='margin-right:5px;'><a target='_blank' href='"
                        + clientHost + permalink + "'>" + clientName + "</a></i></span></p>";
            }

            article.put(Article.ARTICLE_T_IS_BROADCAST, isBroadcast);
        }

        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, clientHost + permalink);

        if (!Role.ADMIN_ROLE.equals(user.optString(User.USER_ROLE))) {
            articleTags = articleMgmtService.filterReservedTags(articleTags);
        }

        try {
//            articleTags = "同步," + articleTags;
            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);

            if (toAdd) {
                articleMgmtService.addArticle(article);
            } else {
                articleMgmtService.updateArticle(article);
            }

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }

        // Updates client record
        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, 0L);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }




}
