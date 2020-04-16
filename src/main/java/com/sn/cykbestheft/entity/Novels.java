package com.sn.cykbestheft.entity;

import lombok.*;

import java.util.Date;

/**
 * @author: songning
 * @date: 2020/3/9 22:17
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Novels {

    private String novelsId;

    private String title;

    private String author;

    private String category;

    private String introduction;

    private String latestChapter;

    private String coverUrl;

    private Date updateTime;

    private Long createTime;

    private String sourceUrl;

    private String sourceName;
}
