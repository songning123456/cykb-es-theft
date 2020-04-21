package com.sn.cykbestheft;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class CykbEsTheftApplicationTests {

    @Autowired
    private ElasticSearchDao elasticSearchDao;

    private ElasticSearch novelsElasticSearch = ElasticSearch.builder().index("novels_index").type("novels").build();
    private ElasticSearch chaptersElasticSearch = ElasticSearch.builder().index("chapters_index").type("chapters").build();

    @Test
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
                        Novels novels = Novels.builder().title(title).author(author).sourceUrl(contentUrl).sourceName("147小说").category(category).createTime(createTime).coverUrl(coverUrl).introduction(introduction).latestChapter(latestChapter).updateTime(strUpdateTime).build();
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
}
