package com.frankisko.clipcat.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.MetadataRecord;
import com.frankisko.clipcat.dto.MetadataRow;
import com.frankisko.clipcat.entities.Metadata;

@Mapper
public interface MetadataMapper {
    List<MetadataRow> getAllByType(Integer idCollection, String type);

    List<Catalog> getCatalogByType(Integer idCollection, String type);

    MetadataRecord getById(Integer idMetadata);

    void insert(Metadata metadata);

    void insertIfNeeded(Integer idCollection, String name, String type);

    void insertIfNeededBulk(Integer idCollection, List<String> names, String type);

    void update(Metadata metadata);

    void delete(Integer idMetadata);

    void deleteByIdCollection(Integer idCollection);

}