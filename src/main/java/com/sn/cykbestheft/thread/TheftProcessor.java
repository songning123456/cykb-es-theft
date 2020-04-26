package com.sn.cykbestheft.thread;

import com.sn.cykbestheft.elasticsearch.dao.ElasticSearchDao;
import com.sn.cykbestheft.elasticsearch.entity.ElasticSearch;
import com.sn.cykbestheft.entity.Chapters;
import com.sn.cykbestheft.entity.Novels;
import com.sn.cykbestheft.util.DateUtil;
import com.sn.cykbestheft.util.HttpUtil;
import io.searchbox.client.JestResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author: songning
 * @date: 2020/3/29 13:19
 */
@Component
@Slf4j
public class TheftProcessor {

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    private ElasticSearch novelsElasticSearch = ElasticSearch.builder().index("novels_index").type("novels").build();
    private ElasticSearch chaptersElasticSearch = ElasticSearch.builder().index("chapters_index").type("chapters").build();

    @Async("TheftExecutor")
    public void theft147(int sortVal) {
        try {
            String prefixUrl = "http://www.147xiaoshuo.com/sort/";
            String categoryUrl = prefixUrl + sortVal + "/";
            log.info("准备开始获取categoryUrl: {}", categoryUrl);
            Document novelsDoc = HttpUtil.getHtmlFromUrl(categoryUrl, true);
            log.info("获取sortVal:{}, categoryUrl:{} 成功!", sortVal, categoryUrl);
            Elements liElements = novelsDoc.getElementById("main").getElementsByClass("novellist").get(0).getElementsByTag("ul").get(0).getElementsByTag("li");
            for (Element liElement : liElements) {
                try {
                    String aContent = liElement.getElementsByTag("a").get(0).attr("href");
                    String sourceUrl = "http://www.147xiaoshuo.com/" + aContent;
                    log.info("获取sourceUrl {}", sourceUrl);
                    // 判断 是否已经存在，如果存在则跳过
                    Map<String, Object> novelsTermParams = new HashMap<String, Object>(2) {{
                        put("sourceUrl", sourceUrl);
                    }};
                    List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                    if (jNovels != null && !jNovels.isEmpty()) {
                        continue;
                    }
                    Document listDoc = null;
                    try {
                        listDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("获取listDoc fail: {}", e.getMessage());
                    }
                    if (listDoc == null) {
                        log.error("listDoc为null,准备下一本小说");
                        continue;
                    }
                    String coverUrl = listDoc.getElementById("fmimg").getElementsByTag("img").get(0).attr("src");
                    log.info("获取coverUrl {} 成功!", coverUrl);
                    String introduction = listDoc.getElementById("intro").html();
                    log.info("获取introduction {} 成功!", introduction);
                    String author = listDoc.getElementById("info").getElementsByTag("p").get(0).html().split("：")[1];
                    log.info("获取author {} 成功!", author);
                    String latestChapter = listDoc.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                    log.info("获取latestChapter {} 成功!", latestChapter);
                    Thread.sleep(1000);
                    Long createTime = DateUtil.dateToLong(new Date());
                    String title = listDoc.getElementById("info").getElementsByTag("h1").get(0).html();
                    log.info("获取title {} 成功!", title);
                    String category = listDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(1).html();
                    log.info("获取category {} 成功!", category);
                    String strUpdateTime = listDoc.getElementById("info").getElementsByTag("p").get(2).html().split("：")[1];
                    log.info("获取novelsUpdateTime {} 成功!", strUpdateTime);
                    Novels novels = Novels.builder().title(title).author(author).sourceUrl(sourceUrl).sourceName("147小说").status("已完结").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(strUpdateTime).build();
                    JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                    String novelsId = ((DocumentResult) jestResult).getId();
                    log.info("NOVELS当前小说title: {}, author: {}, sourceUrl: {}", novels.getTitle(), novels.getAuthor(), novels.getSourceUrl());
                    Elements ddElements = listDoc.getElementById("list").getElementsByTag("dd");
                    for (int k = 0, kLen = ddElements.size(); k < kLen; k++) {
                        try {
                            Element chapterElement = ddElements.get(k).getElementsByTag("a").get(0);
                            String chapter = chapterElement.html();
                            log.info("获取chapter {} 成功!", chapter);
                            String contentUrl = "http://www.147xiaoshuo.com/" + chapterElement.attr("href");
                            log.info("获取contentUrl {} 成功!", contentUrl);
                            String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                            log.info("获取chapterUpTime {} 成功!", chapterUpTime);
                            Chapters chapters = Chapters.builder().chapter(chapter).content("暂无资源...").novelsId(novelsId).updateTime(chapterUpTime).contentUrl(contentUrl).build();
                            elasticSearchDao.save(chaptersElasticSearch, chapters);
                            log.info("CHAPTERS当前小说title: {}, author: {}, sourceUrl: {}; 章节chapter: {}", novels.getTitle(), novels.getAuthor(), novels.getSourceUrl(), chapters.getChapter());
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("147小说 one fail: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("147小说 two fail: {}", e.getMessage());
                }
            }
            log.info("爬取categoryUrl: {}完毕!", categoryUrl);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("爬取147小说sortVal: {} fail:", sortVal);
        }
    }

    @Async("TheftExecutor")
    public void theftBiquge(Element ulElement) {
        for (int j = 0, jLen = ulElement.getElementsByTag("a").size(); j < jLen; j++) {
            try {
                String sourceUrl = ulElement.getElementsByTag("a").get(j).attr("href");
                Map<String, Object> novelsTermParams = new HashMap<String, Object>(2) {{
                    put("sourceUrl", sourceUrl);
                }};
                List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                if (jNovels != null && !jNovels.isEmpty()) {
                    continue;
                }
                log.info("~~~first compare: sourceUrl书库里不存在当前小说，继续爬虫!~~~");
                Document listDoc = null;
                try {
                    listDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("获取listDoc fail: {}", e.getMessage());
                }
                if (listDoc == null) {
                    log.error("listDoc为null,准备下一本小说");
                    continue;
                }
                Element maininfoElement = listDoc.getElementById("maininfo");
                Element infoElement = maininfoElement.getElementById("info");
                String title = infoElement.getElementsByTag("h1").html();
                log.info("获取title {} 成功!", title);
                String author = infoElement.getElementsByTag("p").get(0).html().split("：")[1];
                log.info("获取author {} 成功!", author);
                // 当 书名 和 作者 一致时 代表这本书已经存在，所以不需要
                Map<String, Object> againTermParams = new HashMap<String, Object>(2) {{
                    put("title", title);
                    put("author", author);
                }};
                List<SearchResult.Hit<Object, Void>> againNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, againTermParams, null);
                if (againNovels != null && !againNovels.isEmpty()) {
                    continue;
                }
                log.info("~~~second compare: title-author书库里不存在当前小说，继续爬虫!~~~");
                String coverUrl = listDoc.getElementById("sidebar").getElementsByTag("img").get(0).attr("src");
                log.info("获取coverUrl {} 成功!", coverUrl);
                String introduction = maininfoElement.getElementById("intro").getElementsByTag("p").get(1).html();
                log.info("获取introduction {} 成功!", introduction);
                String latestChapter = infoElement.getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                log.info("获取latestChapter {} 成功!", latestChapter);
                Thread.sleep(1);
                Long createTime = DateUtil.dateToLong(new Date());
                String category = listDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(2).html();
                category = category.replaceAll("小说", "");
                log.info("获取category {} 成功!", category);
                String strUpdateTime = infoElement.getElementsByTag("p").get(2).html().split("：")[1];
                log.info("获取novelsUpdateTime {} 成功!", strUpdateTime);
                Novels novels = Novels.builder().title(title).author(author).sourceUrl(sourceUrl).sourceName("笔趣阁").status("已完结").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(strUpdateTime).build();
                JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                String novelsId = ((DocumentResult) jestResult).getId();
                log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                Element dlElement = listDoc.getElementById("list").getElementsByTag("dl").get(0);
                for (int k = 0, kLen = dlElement.getElementsByTag("dd").size(); k < kLen; k++) {
                    try {
                        Element a = dlElement.getElementsByTag("dd").get(k).getElementsByTag("a").get(0);
                        String chapter = a.html();
                        log.info("获取chapter {} 成功!", chapter);
                        String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                        String contentUrl = "http://www.xbiquge.la/" + a.attr("href");
                        log.info("获取contentUrl {} 成功!", contentUrl);
                        Chapters chapters = Chapters.builder().chapter(chapter).contentUrl(contentUrl).content("暂无资源...").novelsId(novelsId).updateTime(chapterUpTime).build();
                        elasticSearchDao.save(chaptersElasticSearch, chapters);
                        log.info("CHAPTERS当前小说sourceUrl: {}; 章节chapter: {}", novels.getSourceUrl(), chapters.getChapter());
                    } catch (Exception e) {
                        log.error("笔趣阁 one: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("笔趣阁 two: {}", e.getMessage());
            }
        }
        log.info("爬取笔趣阁someone完毕!");
    }

    @Async("TheftExecutor")
    public void theftQushuba(Element ulElement) {
        try {
            for (int j = 0, jLen = ulElement.getElementsByTag("a").size(); j < jLen; j++) {
                String sourceUrl = ulElement.getElementsByTag("a").get(j).attr("href");
                // 判断 是否已经存在，如果存在则跳过
                Map<String, Object> novelsTermParams = new HashMap<String, Object>(2) {{
                    put("sourceUrl", sourceUrl);
                }};
                List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                if (jNovels != null && !jNovels.isEmpty()) {
                    continue;
                }
                log.info("~~~first compare: sourceUrl书库里不存在当前小说，继续爬虫!~~~");
                Document listDoc = null;
                try {
                    listDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("获取listDoc fail: {}", e.getMessage());
                }
                if (listDoc == null) {
                    log.error("listDoc为null,准备下一本小说");
                    continue;
                }
                Element maininfoElement = listDoc.getElementById("maininfo");
                Element infoElement = maininfoElement.getElementById("info");
                String title = infoElement.getElementsByTag("h1").html();
                log.info("获取title {} 成功!", title);
                String author = infoElement.getElementsByTag("p").get(0).html().split("：")[1];
                log.info("获取author {} 成功!", author);
                // 当 书名 和 作者 一致时 代表这本书已经存在，所以不需要
                Map<String, Object> againTermParams = new HashMap<String, Object>(2) {{
                    put("title", title);
                    put("author", author);
                }};
                List<SearchResult.Hit<Object, Void>> againNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, againTermParams, null);
                if (againNovels != null && !againNovels.isEmpty()) {
                    continue;
                }
                log.info("~~~second compare: title-author书库里不存在当前小说，继续爬虫!~~~");
                String coverUrl = listDoc.getElementById("sidebar").getElementsByTag("img").get(0).attr("src");
                log.info("获取coverUrl {} 成功!", coverUrl);
                String introduction = maininfoElement.getElementById("intro").getElementsByTag("p").get(0).html();
                log.info("获取introduction {} 成功!", introduction);
                String latestChapter = infoElement.getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                log.info("获取latestChapter {} 成功!", latestChapter);
                Thread.sleep(1);
                Long createTime = DateUtil.dateToLong(new Date());
                String category = listDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(2).html();
                category = category.replaceAll("小说", "");
                log.info("获取category {} 成功!", category);
                String[] times = (infoElement.getElementsByTag("p").get(2).html().split("：")[1]).split(" ");
                String strUpdateTime = times[0] + " " + times[1] + ":00";
                log.info("获取novelsUpdateTime {} 成功!", strUpdateTime);
                Novels novels = Novels.builder().title(title).author(author).sourceUrl(sourceUrl).sourceName("趣书吧").status("已完结").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(strUpdateTime).build();
                JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                String novelsId = ((DocumentResult) jestResult).getId();
                log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                Element dlElement = listDoc.getElementById("list").getElementsByTag("dl").get(0);
                for (int k = 0, kLen = dlElement.getElementsByTag("dd").size(); k < kLen; k++) {
                    try {
                        Element a = dlElement.getElementsByTag("dd").get(k).getElementsByTag("a").get(0);
                        String chapter = a.html();
                        log.info("获取chapter {} 成功!", chapter);
                        String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                        String contentUrl = sourceUrl + a.attr("href");
                        log.info("获取contentUrl {} 成功!", contentUrl);
                        Chapters chapters = Chapters.builder().chapter(chapter).contentUrl(contentUrl).content("暂无资源...").novelsId(novelsId).updateTime(chapterUpTime).build();
                        elasticSearchDao.save(chaptersElasticSearch, chapters);
                        log.info("CHAPTERS当前小说sourceUrl: {}; 章节chapter: {}", novels.getSourceUrl(), chapters.getChapter());
                    } catch (Exception e) {
                        log.error("趣书吧 one fail: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("趣书吧 two fail: {}", e.getMessage());
        }
        log.info("爬取趣书吧someone完毕!");
    }
}
