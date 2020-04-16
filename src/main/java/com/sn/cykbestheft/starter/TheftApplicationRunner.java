package com.sn.cykbestheft.starter;

import com.sn.cykbestheft.thread.TheftProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author: songning
 * @date: 2020/4/11 10:48
 */
@Component
@Slf4j
@Order(1)
public class TheftApplicationRunner implements ApplicationRunner {

    @Autowired
    private TheftProcessor theftProcessor;

    @Override
    public void run(ApplicationArguments args) {
        log.info("准备开始爬取小说!!!");
        try {
            theftProcessor.theftBiquge();
            theftProcessor.theft147();
            theftProcessor.theftTtsb();
            theftProcessor.theftQushuba();
            theftProcessor.theftFeiku();
        } catch (Exception e) {
            log.error("爬取小说异常: {}", e.getMessage());
        }
    }
}
