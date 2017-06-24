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
package org.b3log.symphony.service;

import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import javax.inject.Inject;
import jodd.io.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Post (article/comment) export service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jul 21, 2016
 * @since 1.4.0
 */
@Service
public class PostExportService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PostExportService.class.getName());

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Exports all posts of a user's specified with the given user id.
     *
     * @param userId the given user id
     * @return download URL, returns {@code "-1"} if in sufficient balance, returns {@code null} if other exceptions
     */
    public String exportPosts(final String userId) {
        final int pointDataExport = Symphonys.getInt("pointDataExport");
        try {
            final JSONObject user = userRepository.get(userId);
            final int balance = user.optInt(UserExt.USER_POINT);

            if (balance - pointDataExport < 0) {
                return "-1";
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Checks user failed", e);

            return null;
        }

        final JSONArray posts = new JSONArray();

        Query query = new Query().setPageCount(-1).setFilter(
                new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId)).
                addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_CONTENT, String.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class);

        try {
            final JSONArray articles = articleRepository.get(query).optJSONArray(Keys.RESULTS);

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.getJSONObject(i);
                final JSONObject post = new JSONObject();

                post.put("id", article.optString(Keys.OBJECT_ID));

                final JSONObject content = new JSONObject();
                content.put("title", article.optString(Article.ARTICLE_TITLE));
                content.put("tags", article.optString(Article.ARTICLE_TAGS));
                content.put("body", article.optString(Article.ARTICLE_CONTENT));

                post.put("content", content.toString());
                post.put("created", Article.ARTICLE_CREATE_TIME);
                post.put("type", "article");

                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Export articles failed", e);

            return null;
        }

        query = new Query().setPageCount(-1).setFilter(
                new PropertyFilter(Comment.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId)).
                addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Comment.COMMENT_CONTENT, String.class).
                addProjection(Comment.COMMENT_CREATE_TIME, Long.class);

        try {
            final JSONArray comments = commentRepository.get(query).optJSONArray(Keys.RESULTS);

            for (int i = 0; i < comments.length(); i++) {
                final JSONObject comment = comments.getJSONObject(i);
                final JSONObject post = new JSONObject();

                post.put("id", comment.optString(Keys.OBJECT_ID));

                final JSONObject content = new JSONObject();
                content.put("title", "");
                content.put("tags", "");
                content.put("body", comment.optString(Comment.COMMENT_CONTENT));

                post.put("content", content.toString());
                post.put("created", Comment.COMMENT_CREATE_TIME);
                post.put("type", "comment");

                posts.put(post);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Export comments failed", e);

            return null;
        }

        LOGGER.info("Exporting posts [size=" + posts.length() + "]");

        final boolean succ = null != pointtransferMgmtService.transfer(userId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_DATA_EXPORT, Pointtransfer.TRANSFER_SUM_C_DATA_EXPORT,
                String.valueOf(posts.length()));
        if (!succ) {
            return null;
        }

        final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String fileKey = "export/" + userId + "/" + uuid + ".zip";

        final String tmpDir = System.getProperty("java.io.tmpdir");
        String localFilePath = tmpDir + "/" + uuid + ".json";
        LOGGER.info(localFilePath);
        final File localFile = new File(localFilePath);

        try {
            final byte[] data = posts.toString(2).getBytes("UTF-8");

            OutputStream output = new FileOutputStream(localFile);
            IOUtils.write(data, output);
            IOUtils.closeQuietly(output);

            final File zipFile = ZipUtil.zip(localFile);

            final FileInputStream inputStream = new FileInputStream(zipFile);
            final byte[] zipData = IOUtils.toByteArray(inputStream);

            if (Symphonys.getBoolean("qiniu.enabled")) {
                final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
                final UploadManager uploadManager = new UploadManager();

                uploadManager.put(zipData, fileKey, auth.uploadToken(Symphonys.get("qiniu.bucket")),
                        null, "application/zip", false);

                return Symphonys.get("qiniu.domain") + "/" + fileKey;
            } else {
                final String filePath = Symphonys.get("upload.dir") + fileKey;

                FileUtils.copyFile(zipFile, new File(filePath));

                return Latkes.getServePath() + "/upload/" + fileKey;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Uploading exprted data failed", e);

            return null;
        }
    }
}
