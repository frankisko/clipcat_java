package com.frankisko.clipcat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class CollectionRow  {

    private Integer idCollection;

    private String name;

    private Integer mediaCount;

    private String location;

    private Long updatedAt;

}
