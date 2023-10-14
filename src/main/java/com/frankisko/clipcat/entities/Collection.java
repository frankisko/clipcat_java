package com.frankisko.clipcat.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.ToString;

//import com.frankisko.clipcat.validations.PathExists;

@JsonInclude(Include.NON_NULL)
@Data
@ToString
public class Collection {

    private Integer idCollection;

    private String name;

    private String location;

    private String tree;

    private Long createdAt;

    private Long updatedAt;

    private List<Metadata> metadata;

    private List<Media> media;

}
