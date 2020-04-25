package com.sn.cykbestheft;

import com.sn.cykbestheft.elasticsearch.dao.ElasticSearchDao;
import com.sn.cykbestheft.elasticsearch.entity.ElasticSearch;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class CykbEsTheftApplicationTests {

    @Autowired
    private ElasticSearchDao elasticSearchDao;
    private ElasticSearch novelsElasticSearch = ElasticSearch.builder().index("novels_index").type("novels").build();

    @Test
    public void test() {
       try {
           String title = "牧神记";
           String author = "宅猪";
           Map<String, Object> againTermParams = new HashMap<String, Object>(2) {{
               put("title", title);
               put("author", author);
           }};
           List<SearchResult.Hit<Object, Void>> againNovels = elasticSearchDao.mustTermRangeQuery(novelsElasticSearch, againTermParams, null);
           log.info("是否存在小说: {}", againNovels.size());
       } catch (Exception e) {
           e.printStackTrace();
       }
    }
}
