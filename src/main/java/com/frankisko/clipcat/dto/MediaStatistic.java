package com.frankisko.clipcat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class MediaStatistic  {

    private Integer idMedia;

    private String name;

    private Long size;

    private Long createdAt;

    private Long lastViewed;

    private Long releaseDate;

    private Long viewCount;

    private String url;

    private Long rating;

    private Long tagsCount;

}