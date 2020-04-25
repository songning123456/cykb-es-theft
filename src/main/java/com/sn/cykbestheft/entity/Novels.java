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

    @NonNull
    private String title;

    @NonNull
    private String author;

    @NonNull
    private String category;

    @NonNull
    private String introduction;

    @NonNull
    private String latestChapter;

    @NonNull
    private String coverUrl;

    @NonNull
    private String updateTime;

    @NonNull
    private Long createTime;

    @NonNull
    private String sourceUrl;

    @NonNull
    private String sourceName;

    @Builder.Default
    private String status = "已完结";
}
