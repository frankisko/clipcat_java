package com.frankisko.clipcat.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.frankisko.clipcat.dto.CollectionRow;
import com.frankisko.clipcat.dto.CollectionRecord;
import com.frankisko.clipcat.entities.Collection;

@Mapper
public interface CollectionMapper {

    List<CollectionRow> getAll();

    CollectionRecord getById(Integer idCollection);

    String getLocation(Integer idCollection);

    void insert(Collection collection);

    void update(Integer idCollection, Map<String, Object> params);

    void delete(Integer idCollection);

}
