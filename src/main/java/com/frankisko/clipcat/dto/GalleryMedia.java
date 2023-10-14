package com.frankisko.clipcat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class GalleryMedia {

    private Integer idMedia;

    private Integer idLocation;

    private String location;

    private String name;

    private Long size;

    private String humanSize;

    private Integer duration;

    private Integer humanDuration;

    private Integer rating;

    private Integer scrapped;

    private Integer hasCover;

    private Long createdAt;

    private Long lastViewed;

    private Integer viewCount;

    private Integer idCollection;

    private String collectionLocation;

    private Integer groupsCount;

    private Integer tagsCount;

}
