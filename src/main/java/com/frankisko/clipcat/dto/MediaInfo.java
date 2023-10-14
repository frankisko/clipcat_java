package com.frankisko.clipcat.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class MediaInfo {

    private Integer idMedia;

    private String location;

    private String name;

    private Long size;

    private String humanSize;

    private Integer duration;

    private Integer minutesDuration;

    private Integer scrapped;

    private Long createdAt;

    private Long releaseDate;

    private Long lastViewed;

    private Integer viewCount;

    private Integer idCollection;

    private String collectionName;

    private String description;

    private String url;

    private Integer hasCover;

    private List<Catalog> groupsMetadata;

    private List<Catalog> tagsMetadata;

}
