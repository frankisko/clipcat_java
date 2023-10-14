package com.frankisko.clipcat.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.frankisko.clipcat.dto.MediaInfo;
import com.frankisko.clipcat.dto.MediaDB;
import com.frankisko.clipcat.dto.MediaStatistic;
import com.frankisko.clipcat.dto.MediaToScrap;
import com.frankisko.clipcat.dto.GalleryMedia;
import com.frankisko.clipcat.entities.Media;

@Mapper
public interface MediaMapper {

    List<MediaStatistic> getStatistics(Integer idCollection);

    MediaInfo getInfo(Integer idMedia);

    List<MediaDB> getMediaDB(Integer idCollection);

    void update(Integer idMedia, Map<String, Object> params);

    List<GalleryMedia> getGalleryMedia(Integer idCollection, Map<String, Object> filters);

    void scrapMediaBulk(List<Integer> idsMedia);

    void unscrapAll(Integer idCollection);

    void delete(Integer idMedia);

    void deleteByIdCollection(Integer idCollection);

    void insert(Media media);

    Integer countMediaInCollection(Integer idCollection);

    Integer countPendingMediaInCollection(Integer idCollection);

    MediaToScrap getMediaToScrap(Integer idCollection);

}
