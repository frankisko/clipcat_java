package com.frankisko.clipcat.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class GalleryLocation {

    private Integer idLocation;

    private String sublocation;

    private String location;

    private List<GalleryMedia> media;

}
