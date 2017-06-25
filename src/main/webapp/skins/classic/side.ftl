<#include 'common/person-info.ftl'/>

<#if ADLabel!="">
${ADLabel}
</#if>
<div class="module">
    <div class="module-header">
        <h2>值得看看</h2>
    </div>
    <div class="module-panel">
        <ul class="module-list open-source">
            <li>
                <a target="_blank" href="https://github.com/b3log/solo"><b class="ft-green slogan">【BigData Expo】</b></a>
                <a class="title" target="_blank" href="http://www.bigdata-expo.org/">数博会官网</a>
            </li>
            <li>
                <a target="_blank" href="https://github.com/b3log/wide"><b class="ft-blue slogan">【GzData】</b></a>
                <a class="title" target="_blank" href="http://www.gzdata.com.cn/">云上贵州 </a>
            </li>
        </ul>
    </div>
</div>
<#--
<#if navTrendTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${hotTopicLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tags fn-clear">
            <#list navTrendTags as trendTag>
            <li>
                <a class="btn small" rel="nofollow" href="${servePath}/tag/${trendTag.tagTitle?url('UTF-8')}">${trendTag.tagTitle}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
-->
<#if sideHotArticles?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${hotArticleLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <#list sideHotArticles as hotArticle>
            <li<#if !hotArticle_has_next> class="last"</#if>>
                <#if "someone" != hotArticle.articleAuthorName>
                <a rel="nofollow" href="${servePath}/member/${hotArticle.articleAuthorName}"></#if>
                    <span class="avatar-small tooltipped tooltipped-se slogan"
                        aria-label="${hotArticle.articleAuthorName}"
                          style="background-image:url('${hotArticle.articleAuthorThumbnailURL}-64.jpg?${hotArticle.articleAuthor.userUpdateTime?c}')"></span>
                <#if "someone" != hotArticle.articleAuthorName></a></#if>
                <a rel="nofollow" class="title" title="${hotArticle.articleTitle}" href="${hotArticle.articlePermalink}">${hotArticle.articleTitleEmoj}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if sideTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${recommendedTags}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="tag-desc fn-clear">
            <#list sideTags as tag>
            <li>
                <span>
                    <#if tag.tagIconPath!="">
                    <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" /></#if><a rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                </span>
                <div<#if tag.tagDescription == ''> style="width:auto"</#if>>
                    <div>${tag.tagDescription}</div>
                    <span class="fn-right">
                        <span class="ft-gray">${referenceLabel}</span> 
                        ${tag.tagReferenceCount} &nbsp;
                        <span class="ft-gray">${cmtLabel}</span>
                        ${tag.tagCommentCount}&nbsp;
                    </span>

                </div>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if sideRandomArticles?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${randomArticleLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="module-list">
            <#list sideRandomArticles as randomArticle>
            <li<#if !randomArticle_has_next> class="last"</#if>>
                <#if "someone" != randomArticle.articleAuthorName>
                <a rel="nofollow" href="${servePath}/member/${randomArticle.articleAuthorName}"></#if>
                    <span aria-label="${randomArticle.articleAuthorName}"
                    style="background-image:url('${randomArticle.articleAuthorThumbnailURL}-64.jpg?${randomArticle.articleAuthor.userUpdateTime?c}')"
                    class="avatar-small tooltipped tooltipped-se slogan"></span>
                <#if "someone" != randomArticle.articleAuthorName></a></#if>
                <a class="title" rel="nofollow" title="${randomArticle.articleTitle}" href="${randomArticle.articlePermalink}">${randomArticle.articleTitleEmoj}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
<#if newTags?size!=0>
<div class="module">
    <div class="module-header">
        <h2>
            ${newTagLabel}
        </h2>
    </div>
    <div class="module-panel">
        <ul class="fn-clear tags">
            <#list newTags as newTag>
            <li>
                <a class="tag" rel="nofollow" href="${servePath}/tag/${newTag.tagTitle?url('UTF-8')}">${newTag.tagTitle}</a>
            </li>
            </#list>
        </ul>
    </div>
</div>
</#if>
