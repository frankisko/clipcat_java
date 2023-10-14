package com.frankisko.clipcat.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dao.MediaMetadataMapper;

@Service
public class MediaMetadataService {

    private final MediaMetadataMapper mediaMetadataMapper;

    private final Globals globals;

    public MediaMetadataService(MediaMetadataMapper mediaMetadataMapper, Globals globals) {
        this.mediaMetadataMapper = mediaMetadataMapper;
        this.globals = globals;
    }

    public void insert(Integer idMedia, Integer idMetadata) {
        mediaMetadataMapper.insert(idMedia, idMetadata);
    }

    public void inserts(Integer idMedia, String type, List<String> metadata) {
        mediaMetadataMapper.inserts(globals.getIdCollection(), idMedia, type, metadata);
    }

    public void insertIfNeededBulk(Integer idMedia, String type, List<String> metadata) {
        mediaMetadataMapper.insertIfNeededBulk(globals.getIdCollection(), idMedia, type, metadata);
    }

    public void deleteByIdMediaAndType(Integer idMedia, String type) {
        mediaMetadataMapper.deleteByIdMediaAndType(idMedia, type);
    }

    public void deleteByIdMedia(Integer idMedia) {
        mediaMetadataMapper.deleteByIdMedia(idMedia);
    }

    public void deleteByIdMetadata(Integer idMetadata) {
        mediaMetadataMapper.deleteByIdMetadata(idMetadata);
    }

    public void deleteByIdCollection(Integer idCollection) {
        mediaMetadataMapper.deleteByIdCollection(idCollection);
    }

}