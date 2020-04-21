package com.sn.cykbestheft.starter;

import com.sn.cykbestheft.thread.TheftProcessor;
import lombok.extern.slf4j.Slf4j;
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
        try {
            List<Integer> suffixList = Arrays.asList(1, 2, 3, 4, 6, 7, 10, 11, 8, 12, 9, 5);
            for (Integer integer : suffixList) {
                theftProcessor.theft147(integer);
            }
        } catch (Exception e) {
            log.error("爬取小说异常: {}", e.getMessage());
        }
    }
}
