package com.frankisko.clipcat.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.LocationRecord;
import com.frankisko.clipcat.entities.Location;

@Mapper
public interface LocationMapper {

    LocationRecord getById(Integer idLocation);

    List<Catalog> getCatalog(Integer idCollection);

    void insertIfNeeded(Location location);

    void deleteObsolete();

    void deleteByIdCollection(Integer idCollection);

}
