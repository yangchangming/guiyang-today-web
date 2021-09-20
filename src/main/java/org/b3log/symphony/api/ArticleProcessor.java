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

import jdk.nashorn.internal.runtime.ECMAException;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
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
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.TokenCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Results;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Article processor.
 *
 * <ul>
 * <li>Gets articles with the specified tags (/apis/articles?tags=tag1,tag2&p=1&size=10), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Jan 2, 2016
 * @since 0.2.5
 */
@RequestProcessor
public class ArticleProcessor {

    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class.getName());

    @Inject
    private TagQueryService tagQueryService;

    @Inject
    private ArticleQueryService articleQueryService;

    @Inject
    private UserQueryService userQueryService;

    @Inject
    private ArticleMgmtService articleMgmtService;

    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/apis/articles", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void getTagsArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer().setJSONP(true);
        context.setRenderer(renderer);

        String callback = request.getParameter("callback");
        if (Strings.isEmptyOrNull(callback)) {
            callback = "callback";
        }
        renderer.setCallback(callback);

        final JSONObject ret = new JSONObject();
        renderer.setJSONObject(ret);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }
        String pageSizeStr = request.getParameter("size");
        if (Strings.isEmptyOrNull(pageSizeStr) || !Strings.isNumeric(pageSizeStr)) {
            pageSizeStr = "10";
        }
        final String tagsStr = request.getParameter("tags");
        if (tagsStr==null || "".equals(tagsStr)){
            ret.put(Article.ARTICLE, new ArrayList());
            return;
        }
        final String[] tagTitles = tagsStr.split(",");
        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Integer.valueOf(pageSizeStr);
        final List<JSONObject> interests = articleQueryService.getInterests(pageNum, pageSize, tagTitles);
        ret.put(Article.ARTICLES, interests);
    }

    /**
     * Get articles by specified tag.
     *
     * @param context
     * @param request
     * @param response
     * @param tagTitle
     * @throws Exception
     */
    @RequestProcessing(value = "/api/v1/articles/{tagTitle}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void getArticlesByTag(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
           final String tagTitle) throws Exception{
        final JSONObject tag = tagQueryService.getTagByTitle(tagTitle);
        int currentPage = 1;
        final int pageSize = 25;
        final String page = request.getParameter("page");
        if (Strings.isNumeric(page)) {
            currentPage = Integer.parseInt(page);
        }
        final JSONObject result = new JSONObject();
        result.put(Article.ARTICLES, this.articleQueryService.getArticlesByTag(tag, currentPage, pageSize));
        context.renderJSON(result);
    }

    /**
     * Show detail article by article id.
     *
     * @param context
     * @param request
     * @param response
     * @param articleId
     * @throws Exception
     */
    @RequestProcessing(value = "/api/v1/article/{articleId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception{
        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (article==null){
            JSONObject result = Results.falseResult();
            result.put(Keys.MSG, "No result");
            result.put(Keys.RESULTS, Collections.emptyList());
            context.renderJSON(result);
            return;
        }
        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
        final JSONObject author = userQueryService.getUser(authorId);

        if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
            article.put(Article.ARTICLE_T_AUTHOR_URL, author.optString(User.USER_URL));
            article.put(Article.ARTICLE_T_AUTHOR_INTRO, author.optString(UserExt.USER_INTRO));
        } else {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
            article.put(Article.ARTICLE_T_AUTHOR_URL, "");
            article.put(Article.ARTICLE_T_AUTHOR_INTRO, "");
        }
        article.put(Common.IS_MY_ARTICLE, false);
        article.put(Article.ARTICLE_T_AUTHOR, author);
        article.put(Common.REWARDED, false);
        if (!article.has(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK)) { // for legacy data
            article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, "");
        }

        articleQueryService.processArticleContent(article, request);
        if (!(Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            articleMgmtService.incArticleViewCount(articleId);
        }

        //加载文章评论
        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }
        int cmtViewMode = 0;
        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.getInt("articleCommentsPageSize");
        final int windowSize = Symphonys.getInt("articleCommentsWindowSize");
        final List<JSONObject> articleComments = commentQueryService.getArticleComments(articleId, pageNum, pageSize, cmtViewMode);
        article.put(Article.ARTICLE_T_COMMENTS, (Object) articleComments);
        JSONObject ret = Results.trueResult();
        ret.put(Keys.RESULTS, article);
        context.renderJSON(ret);
    }


    /**
     * Add article
     *
     * @param context
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestProcessing(value = "/api/v1/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        context.renderJSON();
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean articleCommentable = true;
        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader("User-Agent");
        final boolean isAnonymous = requestJSONObject.optBoolean(Article.ARTICLE_ANONYMOUS, false);
        final int articleAnonymous = isAnonymous ? Article.ARTICLE_ANONYMOUS_C_ANONYMOUS : Article.ARTICLE_ANONYMOUS_C_PUBLIC;

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_TAGS, articleTags);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(Article.ARTICLE_UA, ua);
        }
        article.put(Article.ARTICLE_ANONYMOUS, articleAnonymous);

        try {
            final JSONObject currentUser = requestJSONObject.optJSONObject(User.USER);
            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Article.ARTICLE_AUTHOR_ID));
            final String authorEmail = currentUser.optString(User.USER_EMAIL);
            article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);
            articleMgmtService.addArticle(article);
            context.renderTrueResult();
        }catch (final Exception e){
            context.renderFalseResult();
            final String msg = e.getCause().getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());
            context.renderMsg(msg);
        }
    }



    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void getArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        int currentPage = 1;
        final int pageSize = 25;
        final String page = request.getParameter("page");
        if (Strings.isNumeric(page)) {
            currentPage = Integer.parseInt(page);
        }
        final JSONObject ret = new JSONObject();
        ret.put("stories", this.articleQueryService.getTopArticlesWithComments(currentPage, pageSize));
        context.renderJSON(ret);
    }

    /**
     * Gets articles.with the specified tags.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/recent", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void getRecentArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        int currentPage = 1;
        final int pageSize = 25;
        final String page = request.getParameter("page");
        if (Strings.isNumeric(page)) {
            currentPage = Integer.parseInt(page);
        }
        final JSONObject ret = new JSONObject();
        ret.put("stories", this.articleQueryService.getRecentArticlesWithComments(currentPage, pageSize));
        context.renderJSON(ret);
    }

    /**
     * Gets articles with the specified query.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/api/v1/stories/search", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void searchArticles(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        getRecentArticles(context, request, response);
    }
}
