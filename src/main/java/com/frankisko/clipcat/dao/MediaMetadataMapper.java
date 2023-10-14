package com.frankisko.clipcat.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MediaMetadataMapper {

    void insert(Integer idMedia, Integer idMetadata);

    void inserts(Integer idCollection, Integer idMedia, String type, List<String> metadata);

    void insertIfNeededBulk(Integer idCollection, Integer idMedia, String type, List<String> metadata);

    void deleteByIdMediaAndType(Integer idMedia, String type);

    void deleteByIdMedia(Integer idMedia);

    void deleteByIdMetadata(Integer idMetadata);

    void deleteByIdCollection(Integer idCollection);

}