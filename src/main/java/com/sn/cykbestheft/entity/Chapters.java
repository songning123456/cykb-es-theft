package com.sn.cykbestheft.entity;

import lombok.*;

/**
 * @author: songning
 * @date: 2020/3/9 22:30
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Chapters {

    private String chapterId;

    @NonNull
    private String chapter;

    @NonNull
    private String content;

    @NonNull
    private String novelsId;

    @NonNull
    private String updateTime;

    @NonNull
    private String contentUrl;
}
