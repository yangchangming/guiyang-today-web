<div class="footer">
    <div class="wrapper">
        <div class="fn-flex-1">
            <div class="footer-nav fn-clear">
                <#--<a rel="help" href="https://hacpai.com/article/1440573175609">${aboutLabel}</a>-->
                <#--<a href="https://hacpai.com/article/1457158841475">API</a>-->
                <a href="${servePath}/tag/系统公告">${symAnnouncementLabel}</a>
                <#--<a href="${servePath}/tag/Q%26A">${qnaLabel}</a>-->
                <a href="${servePath}/domains">${domainLabel}</a>
                <a href="${servePath}/tags">${tagLabel}</a>
                <#--<a href="https://hacpai.com/article/1460083956075">${adDeliveryLabel}</a>-->
                <a href="${servePath}/statistic" class="last">${dataStatLabel}</a>

                <div class="fn-right">
                    <span class="ft-gray">&COPY; ${year}</span>
                    <a rel="copyright" href="http://vns.ink" target="_blank">vns.ink</a>
                    ${visionLabel}</div>
            </div>
            <div class="fn-clear">
                <div class="fn-left info responsive-hide">
                    <span class="ft-gray">${onlineVisitorCountLabel}</span> ${onlineVisitorCnt?c} &nbsp;
                    <span class="ft-gray">${maxOnlineVisitorCountLabel}</span> ${statistic.statisticMaxOnlineVisitorCount?c} &nbsp;
                    <span class="ft-gray">${memberLabel}</span> ${statistic.statisticMemberCount?c} &nbsp;
                    <span class="ft-gray">${articleLabel}</span> ${statistic.statisticArticleCount?c} &nbsp;
                    <span class="ft-gray">${domainLabel}</span> ${statistic.statisticDomainCount?c} &nbsp;
                    <span class="ft-gray">${tagLabel}</span> ${statistic.statisticTagCount?c} &nbsp;
                    <span class="ft-gray">${cmtLabel}</span> ${statistic.statisticCmtCount?c}
                </div>
                <div class="fn-right">
                    <span class="ft-gray">Powered by <a href="https://github.com/b3log/symphony" target="_blank">Sym</a>
                        ${version} • ${elapsed?c}ms <a href="https://beian.miit.gov.cn" target="_blank">黔ICP备18004359号</a></span>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="icon-up" onclick="Util.goTop()"></div>
<script type="text/javascript" src="${staticServePath}/js/lib/compress/libs.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/common${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    var Label = {
        invalidPasswordLabel: "${invalidPasswordLabel}",
        loginNameErrorLabel: "${loginNameErrorLabel}",
        followLabel: "${followLabel}",
        unfollowLabel: "${unfollowLabel}",
        symphonyLabel: "${symphonyLabel}",
        visionLabel: "${visionLabel}",
        cmtLabel: "${cmtLabel}",
        collectLabel: "${collectLabel}",
        uncollectLabel: "${uncollectLabel}",
        desktopNotificationTemplateLabel: "${desktopNotificationTemplateLabel}",
        servePath: "${servePath}",
        staticServePath: "${staticServePath}",
        isLoggedIn: ${isLoggedIn?c}
    };
    Util.init(${isLoggedIn?c});
    
    <#if isLoggedIn>
    // Init [User] channel
    Util.initUserChannel("${wsScheme}://${serverHost}:${serverPort}${contextPath}/user-channel");
    </#if>
</script>
<#if algoliaEnabled>
<script src="${staticServePath}/js/lib/algolia/algolia.min.js"></script>
<script>
    Util.initSearch('${algoliaAppId}', '${algoliaSearchKey}', '${algoliaIndex}');
</script>
</#if>
