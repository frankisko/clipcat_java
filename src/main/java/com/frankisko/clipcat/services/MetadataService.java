package com.frankisko.clipcat.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dao.MetadataMapper;
import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.MetadataRecord;
import com.frankisko.clipcat.dto.MetadataRow;
import com.frankisko.clipcat.entities.Collection;
import com.frankisko.clipcat.entities.Metadata;

@Service
public class MetadataService {

    private final MediaMetadataService mediaMetadataService;

    private final MetadataMapper metadataMapper;

    private final Globals globals;

    public MetadataService(MediaMetadataService mediaMetadataService, MetadataMapper metadataMapper, Globals globals) {
        this.mediaMetadataService = mediaMetadataService;
        this.metadataMapper = metadataMapper;
        this.globals = globals;
    }

    public List<MetadataRow> getAllByType(String type) {
        return metadataMapper.getAllByType(globals.getIdCollection(), type);
    }

    public List<Catalog> getCatalogByType(String type) {
        return metadataMapper.getCatalogByType(globals.getIdCollection(), type);
    }

    public void insert(MetadataRecord record) {
        Long epochSeconds = System.currentTimeMillis() / 1000;

        Collection collection = new Collection();
        collection.setIdCollection(globals.getIdCollection());

        Metadata metadata = new Metadata();
        metadata.setName(record.getName());
        metadata.setType(record.getType());
        metadata.setCollection(collection);
        metadata.setCreatedAt(epochSeconds);
        metadata.setUpdatedAt(epochSeconds);

        metadataMapper.insert(metadata);
    }

    public void insertIfNeeded(String name, String type) {
        metadataMapper.insertIfNeeded(globals.getIdCollection(), name, type);
    }

    public void insertIfNeededBulk(List<String> names, String type) {
        metadataMapper.insertIfNeededBulk(globals.getIdCollection(), names, type);
    }

    public MetadataRecord getById(Integer idMetadata) {
        return metadataMapper.getById(idMetadata);
    }

    public void update(Integer idMetadata, MetadataRecord record) {
        Metadata metadata = new Metadata();
        metadata.setIdMetadata(idMetadata);
        metadata.setName(record.getName());
        metadata.setUpdatedAt(System.currentTimeMillis() / 1000);

        metadataMapper.update(metadata);
    }

    public void delete(Integer idMetadata) {
        mediaMetadataService.deleteByIdMetadata(idMetadata);
        metadataMapper.delete(idMetadata);
    }

    public void deleteByIdCollection(Integer idCollection) {
        metadataMapper.deleteByIdCollection(idCollection);
    }

}