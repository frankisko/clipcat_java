package com.frankisko.clipcat.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dao.LocationMapper;
import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.LocationRecord;
import com.frankisko.clipcat.entities.Location;

@Service
public class LocationService {
    @Value("${data_directory}")
    private String dataDirectory;

    private final LocationMapper locationMapper;

    private final Globals globals;

    public LocationService (LocationMapper locationMapper, Globals globals) {
        this.locationMapper = locationMapper;
        this.globals = globals;
    }

    public LocationRecord getById(Integer idLocation) {
        return locationMapper.getById(idLocation);
    }

    public List<Catalog> getCatalog() {
        return locationMapper.getCatalog(globals.getIdCollection());
    }

    public void insertIfNeeded(Location location) {
        locationMapper.insertIfNeeded(location);
    }

    public void deleteObsolete() {
        locationMapper.deleteObsolete();
    }

    public void deleteByIdCollection(Integer idCollection) {
        locationMapper.deleteByIdCollection(idCollection);
    }

    public void open(String location) {
        try {
            //open location
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = new ArrayList<>();

            String OS = System.getProperty("os.name").toLowerCase();

            if (OS.indexOf("win") >= 0) {
                command.add("explorer.exe");
            }
            command.add(location);

            processBuilder.command(command);

            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getAppDataPath () {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory);
    }

    public Path getAppDataTmpPath () {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "tmp");
    }

    public Path getAppDataCollectionPath (Integer idCollection) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString());
    }

    public Path getAppDataThumbnailPath (Integer idCollection) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "thumbnails");
    }

    public Path getAppDataThumbnailMediaPath (Integer idCollection, Integer idMedia) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "thumbnails", idMedia.toString() + ".jpg");
    }

    public Path getAppDataCoverPath (Integer idCollection) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "covers");
    }

    public Path getAppDataCoverMediaPath (Integer idCollection, Integer idMedia) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "covers", idMedia.toString() + ".jpg");
    }

    public Path getAppDataPreviewPath (Integer idCollection) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "previews");
    }

    public Path getAppDataPreviewMediaPath (Integer idCollection, Integer idMedia) {
        return Paths.get(System.getProperty("user.home"), "Documents", dataDirectory, "collections", idCollection.toString(), "previews", idMedia.toString() + ".mp4");
    }
}
