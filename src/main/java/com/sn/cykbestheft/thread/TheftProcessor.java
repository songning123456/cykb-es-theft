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

    @Async("CategoryExecutor")
    public void theftBiquge() {
        try {
            String source = "http://www.xbiquge.la/xiaoshuodaquan/";
            Document html = HttpUtil.getHtmlFromUrl(source, true);
            Element mainElement = html.getElementById("main");
            for (int i = 0, iLen = mainElement.getElementsByClass("novellist").size(); i < iLen; i++) {
                try {
                    Element ulElement = mainElement.getElementsByClass("novellist").get(i).getElementsByTag("ul").get(0);
                    for (int j = 0, jLen = ulElement.getElementsByTag("a").size(); j < jLen; j++) {
                        try {
                            String bookUrl = ulElement.getElementsByTag("a").get(j).attr("href");
                            // 判断 是否已经存在，如果存在则跳过
                            Map<String, Object> novelsTermParams = new HashMap<String, Object>() {
                                {
                                    put("sourceUrl", bookUrl);
                                }
                            };
                            List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                            if (jNovels != null && !jNovels.isEmpty()) {
                                continue;
                            }
                            Document childDoc = HttpUtil.getHtmlFromUrl(bookUrl, true);
                            Element maininfoElement = childDoc.getElementById("maininfo");
                            String coverUrl = childDoc.getElementById("sidebar").getElementsByTag("img").get(0).attr("src");
                            Element infoElement = maininfoElement.getElementById("info");
                            String introduction = maininfoElement.getElementById("intro").getElementsByTag("p").get(1).html();
                            String author = infoElement.getElementsByTag("p").get(0).html().split("：")[1];
                            String latestChapter = infoElement.getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                            Thread.sleep(1);
                            Long createTime = DateUtil.dateToLong(new Date());
                            String title = infoElement.getElementsByTag("h1").html();
                            String category = childDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(2).html();
                            String strUpdateTime = infoElement.getElementsByTag("p").get(2).html().split("：")[1];
                            Date updateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                            Novels novels = Novels.builder().title(title).author(author).sourceUrl(bookUrl).sourceName("笔趣阁").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(updateTime).build();
                            JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                            String novelsId = ((DocumentResult)jestResult).getId();
                            log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                            Element dlElement = childDoc.getElementById("list").getElementsByTag("dl").get(0);
                            for (int k = 0, kLen = dlElement.getElementsByTag("dd").size(); k < kLen; k++) {
                                try {
                                    Element a = dlElement.getElementsByTag("dd").get(k).getElementsByTag("a").get(0);
                                    String chapter = a.html();
                                    Map<String, Object> chaptersTermParams = new HashMap<String, Object>() {
                                        {
                                            put("chapter", chapter);
                                            put("novelsId", novelsId);
                                        }
                                    };
                                    List<SearchResult.Hit<Object, Void>> kChapters = elasticSearchDao.mustTermRangeQuery(chaptersElasticSearch, chaptersTermParams, null);
                                    if (kChapters != null && kChapters.size() > 0) {
                                        continue;
                                    }
                                    String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                                    String chapterUrl = "http://www.xbiquge.la/" + a.attr("href");
                                    Document contentDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                                    String content = contentDoc.getElementById("content").html();
                                    Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(chapterUpTime).build();
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
                } catch (Exception e) {
                    log.error("笔趣阁 three: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("笔趣阁 four: {}", e.getMessage());
        }
    }

    @Async("CategoryExecutor")
    public void theft147() {
        String prefixUrl = "http://www.147xiaoshuo.com/sort/";
        List<Integer> suffixList = Arrays.asList(1, 2, 3, 4, 6, 7, 10, 11, 8, 12, 9, 5);
        for (Integer integer : suffixList) {
            try {
                String fullUrl = prefixUrl + integer + "/";
                Document novelsDoc = HttpUtil.getHtmlFromUrl(fullUrl, true);
                Elements liElements = novelsDoc.getElementById("main").getElementsByClass("novellist").get(0).getElementsByTag("ul").get(0).getElementsByTag("li");
                for (Element liElement : liElements) {
                    try {
                        String AContent = liElement.getElementsByTag("a").get(0).attr("href");
                        String contentUrl = "http://www.147xiaoshuo.com/" + AContent;
                        // 判断 是否已经存在，如果存在则跳过
                        Map<String, Object> novelsTermParams = new HashMap<String, Object>() {
                            {
                                put("sourceUrl", contentUrl);
                            }
                        };
                        List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                        if (jNovels != null && !jNovels.isEmpty()) {
                            continue;
                        }
                        Document contentDoc = HttpUtil.getHtmlFromUrl(contentUrl, true);
                        String coverUrl = contentDoc.getElementById("fmimg").getElementsByTag("img").get(0).attr("src");
                        String introduction = contentDoc.getElementById("intro").html();
                        String author = contentDoc.getElementById("info").getElementsByTag("p").get(0).html().split("：")[1];
                        String latestChapter = contentDoc.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                        Thread.sleep(1);
                        Long createTime = DateUtil.dateToLong(new Date());
                        String title = contentDoc.getElementById("info").getElementsByTag("h1").get(0).html();
                        String category = contentDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(1).html();
                        String strUpdateTime = contentDoc.getElementById("info").getElementsByTag("p").get(2).html().split("：")[1];
                        Date updateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                        Novels novels = Novels.builder().title(title).author(author).sourceUrl(contentUrl).sourceName("147小说").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(updateTime).build();
                        JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                        String novelsId = ((DocumentResult)jestResult).getId();
                        log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                        Elements ddElements = contentDoc.getElementById("list").getElementsByTag("dd");
                        for (int k = 0, kLen = ddElements.size(); k < kLen; k++) {
                            try {
                                Element chapterElement = ddElements.get(k).getElementsByTag("a").get(0);
                                String chapter = chapterElement.html();
                                Map<String, Object> chaptersTermParams = new HashMap<String, Object>() {
                                    {
                                        put("chapter", chapter);
                                        put("novelsId", novelsId);
                                    }
                                };
                                List<SearchResult.Hit<Object, Void>> kChapters = elasticSearchDao.mustTermRangeQuery(chaptersElasticSearch, chaptersTermParams, null);
                                if (kChapters != null && kChapters.size() > 0) {
                                    continue;
                                }
                                String chapterUrl = "http://www.147xiaoshuo.com/" + chapterElement.attr("href");
                                String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                                Document chapterDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                                String content = chapterDoc.getElementById("content").html();
                                Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(chapterUpTime).build();
                                elasticSearchDao.save(chaptersElasticSearch, chapters);
                                log.info("CHAPTERS当前小说sourceUrl: {}; 章节chapter: {}", novels.getSourceUrl(), chapters.getChapter());
                            } catch (Exception e) {
                                log.error("147小说 one fail: {}", e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.error("147小说 two fail: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("147小说 three fail: {}", e.getMessage());
            }
        }
    }

    @Async("CategoryExecutor")
    public void theftTtsb() {
        String prefixUrl = "http://www.ttshuba.net/fenlei/";
        for (int i = 1; i <= 10; i++) {
            try {
                String fullUrl = prefixUrl + i + "/1.html";
                Document novelsDoc = HttpUtil.getHtmlFromUrl(fullUrl, true);
                this.handleTiantian(novelsDoc);
                // 分页处理后续页面
                int lastPage = Integer.parseInt(novelsDoc.getElementById("pagelink").getElementsByClass("last").get(0).html());
                if (lastPage > 1) {
                    for (int m = 2; m <= lastPage; m++) {
                        String pageUrl = prefixUrl + i + "/" + m + ".html";
                        Document pageNovelsDoc = HttpUtil.getHtmlFromUrl(pageUrl, true);
                        this.handleTiantian(pageNovelsDoc);
                    }
                }
            } catch (Exception e) {
                log.error("天天书吧 three fail: {}", e.getMessage());
            }
        }
    }

    private void handleTiantian(Document document) {
        Elements picElements = document.getElementById("alist").getElementsByClass("pic");
        for (int j = 0, jLen = picElements.size(); j < jLen; j++) {
            try {
                String dictionaryUrl = picElements.get(j).getElementsByTag("a").get(0).attr("href");
                // 判断 是否已经存在，如果存在则跳过
                Map<String, Object> novelsTermParams = new HashMap<String, Object>() {
                    {
                        put("sourceUrl", dictionaryUrl);
                    }
                };
                List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                if (jNovels != null && !jNovels.isEmpty()) {
                    continue;
                }
                String introduction = document.getElementById("alist").getElementsByClass("info").get(j).getElementsByClass("intro").get(0).html();
                Document dictionaryDoc = HttpUtil.getHtmlFromUrl(dictionaryUrl, true);
                String coverUrl = dictionaryDoc.getElementById("fmimg").getElementsByTag("img").get(0).attr("src");
                String author = dictionaryDoc.getElementById("info").getElementsByTag("p").get(0).html().split("：")[1];
                String latestChapter = dictionaryDoc.getElementById("info").getElementsByTag("p").get(5).getElementsByTag("a").html();
                Thread.sleep(1);
                Long createTime = DateUtil.dateToLong(new Date());
                String title = dictionaryDoc.getElementById("info").getElementsByTag("h1").get(0).html();
                String category = dictionaryDoc.getElementById("info").getElementsByTag("p").get(1).html().split("：")[1];
                String strUpdateTime = DateUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss");
                Date updateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                Novels novels = Novels.builder().title(title).author(author).sourceUrl(dictionaryUrl).sourceName("天天书吧").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(updateTime).build();
                JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                String novelsId = ((DocumentResult)jestResult).getId();
                log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                Elements ddElements = dictionaryDoc.getElementById("list").getElementsByTag("dd");
                for (int k = 0, kLen = ddElements.size(); k < kLen; k++) {
                    try {
                        Element chapterElement = ddElements.get(k).getElementsByTag("a").get(0);
                        String chapter = chapterElement.html();
                        Map<String, Object> chaptersTermParams = new HashMap<String, Object>() {
                            {
                                put("chapter", chapter);
                                put("novelsId", novelsId);
                            }
                        };
                        List<SearchResult.Hit<Object, Void>> kChapters = elasticSearchDao.mustTermRangeQuery(chaptersElasticSearch, chaptersTermParams, null);
                        if (kChapters != null && kChapters.size() > 0) {
                            continue;
                        }
                        String chapterUrl = dictionaryUrl + chapterElement.attr("href");
                        String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                        Document chapterDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                        String content = chapterDoc.getElementById("TXT").html();
                        int headIndex = content.indexOf("&nbsp;&nbsp;&nbsp;&nbsp;");
                        int tailIndex = content.indexOf("<div class=\"bottem\">");
                        content = content.substring(headIndex, tailIndex);
                        Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(chapterUpTime).build();
                        elasticSearchDao.save(chaptersElasticSearch, chapters);
                        log.info("CHAPTERS当前小说sourceUrl: {}; 章节chapter: {}", novels.getSourceUrl(), chapters.getChapter());
                    } catch (Exception e) {
                        log.error("天天书吧 one fail: {}", e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("天天书吧 two fail: {}", e.getMessage());
            }
        }
    }

    @Async("CategoryExecutor")
    public void theftQushuba() {
        try {
            String source = "http://www.qushuba.com/xiaoshuodaquan/";
            Document allNovelsDoc = HttpUtil.getHtmlFromUrl(source, true);
            Element mainElement = allNovelsDoc.getElementById("main");
            for (int i = 0, iLen = mainElement.getElementsByClass("novellist").size(); i < iLen; i++) {
                try {
                    Element ulElement = mainElement.getElementsByClass("novellist").get(i).getElementsByTag("ul").get(0);
                    for (int j = 0, jLen = ulElement.getElementsByTag("a").size(); j < jLen; j++) {
                        String novelsUrl = ulElement.getElementsByTag("a").get(j).attr("href");
                        // 判断 是否已经存在，如果存在则跳过
                        Map<String, Object> novelsTermParams = new HashMap<String, Object>() {
                            {
                                put("sourceUrl", novelsUrl);
                            }
                        };
                        List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                        if (jNovels != null && !jNovels.isEmpty()) {
                            continue;
                        }
                        Document novelsDoc = HttpUtil.getHtmlFromUrl(novelsUrl, true);
                        Element maininfoElement = novelsDoc.getElementById("maininfo");
                        String coverUrl = novelsDoc.getElementById("sidebar").getElementsByTag("img").get(0).attr("src");
                        Element infoElement = maininfoElement.getElementById("info");
                        String introduction = maininfoElement.getElementById("intro").getElementsByTag("p").get(0).html();
                        String author = infoElement.getElementsByTag("p").get(0).html().split("：")[1];
                        String latestChapter = infoElement.getElementsByTag("p").get(3).getElementsByTag("a").get(0).html();
                        Thread.sleep(1);
                        Long createTime = DateUtil.dateToLong(new Date());
                        String title = infoElement.getElementsByTag("h1").html();
                        String category = novelsDoc.getElementsByClass("con_top").get(0).getElementsByTag("a").get(2).html();
                        String[] times = (infoElement.getElementsByTag("p").get(2).html().split("：")[1]).split(" ");
                        String strUpdateTime = times[0] + " " + times[1] + ":00";
                        Date updateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                        Novels novels = Novels.builder().title(title).author(author).sourceUrl(novelsUrl).sourceName("趣书吧").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(updateTime).build();
                        JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                        String novelsId = ((DocumentResult)jestResult).getId();
                        log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                        Element dlElement = novelsDoc.getElementById("list").getElementsByTag("dl").get(0);
                        for (int k = 0, kLen = dlElement.getElementsByTag("dd").size(); k < kLen; k++) {
                            try {
                                Element a = dlElement.getElementsByTag("dd").get(k).getElementsByTag("a").get(0);
                                String chapter = a.html();
                                Map<String, Object> chaptersTermParams = new HashMap<String, Object>() {
                                    {
                                        put("chapter", chapter);
                                        put("novelsId", novelsId);
                                    }
                                };
                                List<SearchResult.Hit<Object, Void>> kChapters = elasticSearchDao.mustTermRangeQuery(chaptersElasticSearch, chaptersTermParams, null);
                                if (kChapters != null && kChapters.size() > 0) {
                                    continue;
                                }
                                String chapterUrl = novelsUrl + a.attr("href");
                                String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                                Document contentDoc = HttpUtil.getHtmlFromUrl(chapterUrl, true);
                                String content = contentDoc.getElementById("content").html();
                                Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(chapterUpTime).build();
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
            }
        } catch (Exception e) {
            log.error("趣书吧 three fail: {}", e.getMessage());
        }
    }

    @Async("CategoryExecutor")
    public void theftFeiku() {
        String baseUrl = "http://www.feiku.org/shuku/quanbu_default_0_0_0_0_0_0_";
        for (int i = 0, iLen = 69; i < iLen; i++) {
            try {
                String sourceUrl = baseUrl + (i + 1) + ".html";
                Document pageNovelsDoc = HttpUtil.getHtmlFromUrl(sourceUrl, true);
                Elements liElements = pageNovelsDoc.getElementsByClass("books-list").get(0).getElementsByTag("li");
                for (Element liElement : liElements) {
                    try {
                        Element aElement = liElement.getElementsByTag("a").get(0);
                        String novelsUrl = aElement.attr("href");
                        // 判断 是否已经存在，如果存在则跳过
                        Map<String, Object> novelsTermParams = new HashMap<String, Object>() {
                            {
                                put("sourceUrl", novelsUrl);
                            }
                        };
                        List<SearchResult.Hit<Object, Void>> jNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, novelsTermParams, null);
                        if (jNovels != null && !jNovels.isEmpty()) {
                            continue;
                        }
                        Document novelsDoc = HttpUtil.getHtmlFromUrl(novelsUrl, true);
                        String coverUrl = novelsDoc.getElementsByClass("bookcover-l").get(0).getElementsByTag("img").get(0).attr("src");
                        Element bookInfo = novelsDoc.getElementsByClass("book-intro").get(0);
                        String paramTime = bookInfo.getElementsByTag("b").get(1).html();
                        int left = paramTime.indexOf("(");
                        int right = paramTime.indexOf(")");
                        String strUpdateTime = paramTime.substring(left + 1, right) + " 00:00:00";
                        Date updateTime = DateUtil.strToDate(strUpdateTime, "yyyy-MM-dd HH:mm:ss");
                        String bookHtml = bookInfo.html();
                        int firstBr = bookHtml.indexOf("<br>");
                        String introduction = bookHtml.substring(0, firstBr);
                        introduction = introduction.replaceAll("　　&nbsp;&nbsp;&nbsp;&nbsp;", "");
                        int firstB = bookHtml.indexOf("</b>");
                        int lastBr = bookHtml.lastIndexOf("<br>");
                        String[] info = bookHtml.substring(firstB + 5, lastBr - 2).split(" ");
                        String title = info[0];
                        String author = info[1];
                        String category = info[2];
                        String latestChapter = bookInfo.getElementsByTag("a").html();
                        Thread.sleep(1);
                        Long createTime = DateUtil.dateToLong(new Date());
                        Novels novels = Novels.builder().title(title).author(author).sourceUrl(novelsUrl).sourceName("飞库小说").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(updateTime).build();
                        JestResult jestResult = elasticSearchDao.save(novelsElasticSearch, novels);
                        String novelsId = ((DocumentResult)jestResult).getId();
                        log.info("NOVELS当前小说sourceUrl: {}", novels.getSourceUrl());
                        String listUrl = novelsDoc.getElementsByClass("catalogbtn").get(0).attr("href");
                        Document listDoc = HttpUtil.getHtmlFromUrl(listUrl, true);
                        Elements chapterList = listDoc.getElementsByClass("chapter-list").get(0).getElementsByTag("a");
                        for (int k = 0, kLen = chapterList.size(); k < kLen; k++) {
                            try {
                                String contentUrl = "http://www.feiku.org" + chapterList.get(k).attr("href");
                                String chapter = chapterList.get(k).html();
                                Map<String, Object> chaptersTermParams = new HashMap<String, Object>() {
                                    {
                                        put("chapter", chapter);
                                        put("novelsId", novelsId);
                                    }
                                };
                                List<SearchResult.Hit<Object, Void>> kChapters = elasticSearchDao.mustTermRangeQuery(chaptersElasticSearch, chaptersTermParams, null);
                                if (kChapters != null && kChapters.size() > 0) {
                                    continue;
                                }
                                String chapterUpTime = DateUtil.intervalTime(strUpdateTime, kLen - k - 1);
                                Document contentDoc = HttpUtil.getHtmlFromUrl(contentUrl, true);
                                String content = contentDoc.getElementsByClass("article-con").get(0).html();
                                Chapters chapters = Chapters.builder().chapter(chapter).content(content).novelsId(novelsId).updateTime(chapterUpTime).build();
                                elasticSearchDao.save(chaptersElasticSearch, chapters);
                                log.info("CHAPTERS当前小说sourceUrl: {}; 章节chapter: {}", novels.getSourceUrl(), chapters.getChapter());
                            } catch (Exception e) {
                                log.error("飞库小说 one fail: {}", e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.error("飞库小说 two fail: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("飞库小说 three fail: {}", e.getMessage());
            }
        }
    }
}
