package com.frankisko.clipcat.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import com.frankisko.clipcat.dao.CollectionMapper;
import com.frankisko.clipcat.dto.CollectionRow;
import com.frankisko.clipcat.dto.MediaDB;
import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.CollectionRecord;
import com.frankisko.clipcat.entities.Collection;
import com.frankisko.clipcat.entities.Location;
import com.frankisko.clipcat.entities.Media;

@Service
public class CollectionService {
    @Value("${data_directory}")
    private String dataDirectory;

    private final CollectionMapper collectionMapper;

    private final MediaService mediaService;

    private final MediaMetadataService mediaMetadataService;

    private final MetadataService metadataService;

    private final LocationService locationService;

    public CollectionService(CollectionMapper collectionMapper, MediaService mediaService,
                             MediaMetadataService mediaMetadataService, MetadataService metadataService,
                             LocationService locationService) {
        this.collectionMapper = collectionMapper;
        this.mediaService = mediaService;
        this.mediaMetadataService = mediaMetadataService;
        this.metadataService = metadataService;
        this.locationService = locationService;
    }

    public List<CollectionRow> getAll() {
        return collectionMapper.getAll();
    }

    public void insert(CollectionRecord record) {
        Long epochSeconds = System.currentTimeMillis() / 1000;

        Collection collection = new Collection();
        collection.setName(record.getName());
        collection.setLocation(StringUtils.trimTrailingCharacter(record.getLocation(), File.separatorChar)); //remove trailing slash
        collection.setCreatedAt(epochSeconds);
        collection.setUpdatedAt(epochSeconds);

        collectionMapper.insert(collection);
    }

    public CollectionRecord getById(Integer idCollection) {
        return collectionMapper.getById(idCollection);
    }

    public String getLocation(Integer idCollection) {
        return collectionMapper.getLocation(idCollection);
    }

    public void update(Integer idCollection, CollectionRecord record) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", record.getName());
        params.put("updated_at", System.currentTimeMillis() / 1000);
        collectionMapper.update(idCollection, params);
    }

    public void lastUpdate(Integer idCollection) {
        Map<String, Object> params = new HashMap<>();
        params.put("updated_at", System.currentTimeMillis() / 1000);
        collectionMapper.update(idCollection, params);
    }

    public void delete(Integer idCollection) {
        mediaMetadataService.deleteByIdCollection(idCollection); //delete media_metadata
        mediaService.deleteByIdCollection(idCollection);         //delete media
        metadataService.deleteByIdCollection(idCollection);      //delete metadata
        locationService.deleteByIdCollection(idCollection);      //delete locations
        collectionMapper.delete(idCollection);                   //delete collection

        try {
            //delete thumbnail folder
            FileSystemUtils.deleteRecursively(locationService.getAppDataCollectionPath(idCollection));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scrap(Integer idCollection) {
        //create folders
        //collection/{idCollection}/thumbnails
        File appDataThumbnailsFolder = locationService.getAppDataThumbnailPath(idCollection).toFile();
        appDataThumbnailsFolder.mkdirs();

        //collection/{idCollection}/covers
        File appDataCoversFolder = locationService.getAppDataCoverPath(idCollection).toFile();
        appDataCoversFolder.mkdirs();

        //collection/{idCollection}/previews
        File appDataPreviewsFolder = locationService.getAppDataPreviewPath(idCollection).toFile();
        appDataPreviewsFolder.mkdirs();

        //create tmp folder
        File tmpDataFolder = locationService.getAppDataTmpPath().toFile();
        tmpDataFolder.mkdirs();

        //TODO chunk list

        mediaService.unscrapAll(); //set all media as not scrapped
        this.lastUpdate(idCollection); //set last updated for collection

        //get all files in filesystem
        Path collectionLocation = Paths.get(this.getLocation(idCollection));
        Map<String, Long> fileSystemMap = getFilesInFileSystem(collectionLocation);
        Set<String> fileSystemSet = fileSystemMap.keySet();

        //get all media in db
        Map<String, MediaDB> mediaDBMap = new HashMap<>();

        List<MediaDB> mediaDB = mediaService.getMediaDB();

        for (MediaDB row : mediaDB) {
            mediaDBMap.put(row.getFullLocation(), row);
        }
        Set<String> mediaDBSet = mediaDBMap.keySet();

        //case 1: both files (filesystem) and media (database) = set scrap to true for unchanged files
        scrapUnchanged(fileSystemMap, fileSystemSet, mediaDBMap, mediaDBSet);

        //case 2: file (filesystem) exist, but media (database) doesnt exist = new file
        scrapNew(idCollection, fileSystemMap, fileSystemSet, mediaDBMap, mediaDBSet);

        //case 3: media exist in database, but file does not exist in filesystem = obsolete media
        cleanUp(fileSystemMap, fileSystemSet, mediaDBMap, mediaDBSet);
    }

    public Map<String, Long> getFilesInFileSystem(Path filesSystemPath) {
        Map<String, Long> fileSystemMap = new HashMap<>();

        try {
            Files.walkFileTree(filesSystemPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // This method is called for each file in the directory
                    // Check if the extension is Valid
                    if (mediaService.isValidExtension(file.getFileName().toString())) {
                        fileSystemMap.put(file.toString(), attrs.size());
                        //System.out.println(file.toString());
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // This method is called if there is an error visiting a file
                    System.err.println("Error visiting file: " + file.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSystemMap;
    }

    public void scrapUnchanged(Map<String, Long> fileSystemMap, Set<String> fileSystemSet, Map<String, MediaDB> mediaDBMap, Set<String> mediaDBSet) {
        Set<String> intersectSet = new HashSet<>(fileSystemSet);
        intersectSet.retainAll(mediaDBSet);

        //detect unchanged media (file and media have same size)
        List<Integer> idsMedia = new ArrayList<>();

        for (String intersectPath : intersectSet) {
            Integer idMedia = mediaDBMap.get(intersectPath).getIdMedia();
            if (fileSystemMap.get(intersectPath).equals(mediaDBMap.get(intersectPath).getSize())) {
                idsMedia.add(idMedia);
            } else {
                //if not same size, unscrap media
                mediaService.unscrapMedia(idMedia, true);
            }
        }

        int BATCH_SIZE = 1000;
        int pages = idsMedia.size() /  BATCH_SIZE;

        if (idsMedia.size() % BATCH_SIZE != 0) {
            pages = pages + 1;
        }

        //Process each batch
        IntStream.range(0, pages).forEach((index)->{
            List<Integer> batch = idsMedia.stream()
                                         .skip(index * BATCH_SIZE)
                                         .limit(BATCH_SIZE)
                                         .collect(Collectors.toList());

            mediaService.scrapMediaBulk(batch);
        });
    }

    public void scrapNew(Integer idCollection, Map<String, Long> fileSystemMap, Set<String> fileSystemSet, Map<String, MediaDB> mediaDBMap, Set<String> mediaDBSet) {
        long epochSeconds = System.currentTimeMillis() / 1000;

        Set<String> newFilesSet = new HashSet<>(fileSystemSet);
        newFilesSet.removeAll(mediaDBSet);

        //check what locations are new
        //get for filesystem
        Set<String> filesFileSystemPathsSet = new HashSet<>();
        for (String newFileSet : newFilesSet) {
            filesFileSystemPathsSet.add(newFileSet.substring(0, newFileSet.lastIndexOf("\\")));
        }

        //get for database
        Set<String> mediaDatabaseLocationsSet = new HashSet<>();
        for (String newMediaSet : mediaDatabaseLocationsSet) {
            mediaDatabaseLocationsSet.add(newMediaSet.substring(0, newMediaSet.lastIndexOf("\\")));
        }

        Set<String> newPathsSet = new HashSet<>(filesFileSystemPathsSet);
        newPathsSet.removeAll(mediaDatabaseLocationsSet);

        //insert location if needed
        Collection collection = new Collection();
        collection.setIdCollection(idCollection);

        for (String newPath : newPathsSet) {
            Location location = new Location();
            location.setLocation(newPath);
            location.setCollection(collection);

            locationService.insertIfNeeded(location);
        }

        //get locations catalog
        List<Catalog> locations = locationService.getCatalog();
        Map<String, Integer> locationsCatalogMap = new HashMap<>();

        for (Catalog locationCatalog : locations) {
            locationsCatalogMap.put(locationCatalog.getName(), locationCatalog.getId());
        }

        //insert new files
        for (String newFilePath : newFilesSet) {
            Media newMedia = new Media();
            newMedia.setName(Paths.get(newFilePath).getFileName().toString());

            String mediaLocation = newFilePath.replace("\\" + newMedia.getName(), "");
            Location location = new Location();
            location.setIdLocation(locationsCatalogMap.get(mediaLocation));

            newMedia.setLocation(location);
            newMedia.setSize(0l);
            newMedia.setDuration(0);
            newMedia.setScrapped(0);
            newMedia.setCollection(collection);
            newMedia.setViewCount(0);
            newMedia.setCreatedAt(epochSeconds);
            newMedia.setUpdatedAt(epochSeconds);
            newMedia.setRating(0);

            mediaService.insert(newMedia);
        }
    }

    public void cleanUp(Map<String, Long> fileSystemMap, Set<String> fileSystemSet, Map<String, MediaDB> mediaDBMap, Set<String> mediaDBSet) {
        Set<String> obsoleteMediaSet = new HashSet<>(mediaDBSet);
        obsoleteMediaSet.removeAll(fileSystemSet);

        for (String obsoleteMediaLocation : obsoleteMediaSet) {
            mediaService.deleteThumbnail(mediaDBMap.get(obsoleteMediaLocation).getIdMedia());
            mediaService.delete(mediaDBMap.get(obsoleteMediaLocation).getIdMedia());
        }

        //remove locations without files
        locationService.deleteObsolete();

    }
}