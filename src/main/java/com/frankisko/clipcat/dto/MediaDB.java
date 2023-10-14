package com.frankisko.clipcat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class MediaDB {

    private Integer idMedia;

    private String fullLocation;

    private Long size;

}
