package com.frankisko.clipcat.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class Metadata {

    private Integer idMetadata;

    private String name;

    private String type;

    private Collection collection;

    private Long createdAt;

    private Long updatedAt;

}
