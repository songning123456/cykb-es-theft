package com.sn.cykbestheft.starter;

import com.sn.cykbestheft.thread.TheftProcessor;
import com.sn.cykbestheft.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author: songning
 * @date: 2020/4/11 10:48
 */
@Component
@Slf4j
public class TheftApplicationRunner implements ApplicationRunner {

    @Autowired
    private TheftProcessor theftProcessor;

    @Override
    public void run(ApplicationArguments args) {
        log.info("准备开始爬取小说!!!");
        this.prepareBiquge();
    }

    private void prepare147() {
        try {
            List<Integer> suffixList = Arrays.asList(1, 2, 3, 4, 6, 7, 10, 11, 8, 12, 9, 5);
            for (Integer integer : suffixList) {
                theftProcessor.theft147(integer);
            }
        } catch (Exception e) {
            log.error("爬取147小说异常: {}", e.getMessage());
        }
    }

    private void prepareBiquge() {
        try {
            String source = "http://www.xbiquge.la/xiaoshuodaquan/";
            Document html = HttpUtil.getHtmlFromUrl(source, true);
            log.info("~~~获取笔趣阁全部小说页面成功!~~~");
            Element mainElement = html.getElementById("main");
            for (int i = 0, iLen = mainElement.getElementsByClass("novellist").size(); i < iLen; i++) {
                try {
                    Element ulElement = mainElement.getElementsByClass("novellist").get(i).getElementsByTag("ul").get(0);
                    log.info("准备爬取笔趣阁i: {}", i);
                    theftProcessor.theftBiquge(ulElement);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("笔趣阁 three: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("爬取笔趣阁小说异常: {}", e.getMessage());
        }
    }
}
