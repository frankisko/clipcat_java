package com.frankisko.clipcat.controllers;

import java.util.HashMap;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.controllers.responses.GalleryResponse;
import com.frankisko.clipcat.dto.Catalog;
import com.frankisko.clipcat.dto.MediaInfo;
import com.frankisko.clipcat.dto.MediaRatingRecord;
import com.frankisko.clipcat.dto.MediaStatistic;
import com.frankisko.clipcat.dto.MediaUrlRecord;
import com.frankisko.clipcat.dto.GalleryMedia;
import com.frankisko.clipcat.dto.GalleryLocation;
import com.frankisko.clipcat.services.ApiClientService;
import com.frankisko.clipcat.services.MediaService;
import com.frankisko.clipcat.services.MetadataService;

import groovy.transform.builder.InitializerStrategy.SET;

import com.frankisko.clipcat.services.LocationService;
import com.frankisko.clipcat.services.MediaMetadataService;

@Controller
public class MediaController {
    @Value("${api_url}")
    private String apiUrl;

    private final MediaService mediaService;

    private final MetadataService metadataService;

    private final MediaMetadataService mediaMetadataService;

    private final LocationService locationService;

    private final ApiClientService apiClientService;

    private final Globals globals;

    public MediaController(MediaService mediaService, MetadataService metadataService,
                          MediaMetadataService mediaMetadataService,
                          LocationService locationService, ApiClientService apiClientService,
                          Globals globals) {
        this.mediaService = mediaService;
        this.metadataService = metadataService;
        this.mediaMetadataService = mediaMetadataService;
        this.locationService = locationService;
        this.apiClientService = apiClientService;
        this.globals = globals;
    }

    @GetMapping("/media/index")
    public ModelAndView index() {
        //get all available metadata
        Map<String, List<Catalog>> metadata = new HashMap<String, List<Catalog>>();
        metadata.put("groups", metadataService.getCatalogByType("group"));
        metadata.put("tags", metadataService.getCatalogByType("tag"));

        //handle view
        ModelAndView modelAndView = new ModelAndView("media/index");
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("metadata", metadata);
        modelAndView.addObject("activeMenu", "media");

        return modelAndView;
    }

    @GetMapping("/media/search")
    @ResponseBody
    public GalleryResponse search(
        @RequestParam(value = "text", required = false) String filterText,
        @RequestParam(value = "type", required = false, defaultValue = "name") String filterType,
        @RequestParam(value = "visibility", required = false, defaultValue = "all") String filterVisibility,
        @RequestParam(value = "rating", required = false) List<Integer> filterRating,
        @RequestParam(value = "metadata", required = false) List<Integer> filterMetadata
    ) {
        GalleryResponse galleryResponse = new GalleryResponse();
        Map<Integer, GalleryLocation> galleryMap = new HashMap<>();
        Integer mediaCount = 0;

        //check filters
        HashMap<String, Object> filters = new HashMap<>();
        if (filterText != null) {
            filters.put("text", filterText);
        }
        if (filterType != null) {
            filters.put("type", filterType);
        }
        if (filterVisibility != null) {
            filters.put("visibility", filterVisibility);
        }
        if (filterRating != null) {
            filters.put("rating", filterRating);
        }
        if (!filterMetadata.isEmpty()) {
            filters.put("metadata", filterMetadata);
        }

        List<GalleryMedia> galleryMedia = mediaService.getGalleryMedia(filters);
        for (GalleryMedia row : galleryMedia) {
            GalleryLocation galleryLocation = new GalleryLocation();

            //initialize if not exists
            if (!galleryMap.containsKey(row.getIdLocation())) {
                String sublocation = row.getLocation().replace(row.getCollectionLocation(), "");
                galleryLocation.setSublocation(sublocation);
                galleryLocation.setLocation(row.getLocation());
                galleryLocation.setIdLocation(row.getIdLocation());
                galleryLocation.setMedia(new ArrayList<>());

                galleryMap.put(row.getIdLocation(), galleryLocation);
            } else {
                galleryLocation = galleryMap.get(row.getIdLocation());
            }

            //append media
            Double megabytes = row.getSize() / (1024.0 * 1024.0);
            row.setHumanSize(String.format("%.2f MB", megabytes));

            galleryMap.get(row.getIdLocation()).getMedia().add(row);

            mediaCount++;
        }

        List<GalleryLocation> rows = new ArrayList<GalleryLocation>(galleryMap.values());
        rows = rows.stream()
                   .sorted((o1, o2) -> o1.getSublocation().compareTo(o2.getSublocation()))
                   .collect(Collectors.toList());

        galleryResponse.setRows(rows);
        galleryResponse.setMediaCount(mediaCount);

        return galleryResponse;
    }

    @GetMapping("/media/statistics")
    public ModelAndView statistics() {
        List<MediaStatistic> rows = apiClientService.mediaStatistics();

        ModelAndView modelAndView = new ModelAndView("media/statistics");
        modelAndView.addObject("rows", rows);
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("activeMenu", "statistics");

        return modelAndView;
    }

    @GetMapping("/media/{idMedia}/info")
    public ModelAndView info(@PathVariable(name = "idMedia") Integer idMedia, @RequestParam(name = "tab", required = false, defaultValue = "group") String tab) {
        MediaInfo data = apiClientService.mediaInfo(idMedia);

        Double megabytes = data.getSize() / (1024.0 * 1024.0);
        data.setHumanSize(String.format("%.2f MB", megabytes));

        //get all available metadata
        Map<String, List<Catalog>> metadata = new HashMap<String, List<Catalog>>();
        metadata.put("groups", metadataService.getCatalogByType("group"));
        metadata.put("tags", metadataService.getCatalogByType("tag"));

        //get metadata for clipboard
        Map<String, String> metadataClipboard = new HashMap<String, String>();
        metadataClipboard.put("groups", data.getGroupsMetadata().stream().map(Catalog::getName).collect(Collectors.joining(",")));
        metadataClipboard.put("tags", data.getTagsMetadata().stream().map(Catalog::getName).collect(Collectors.joining(",")));

        //get selected metadata
        Map<String, List<Integer>> metadataSelected = new HashMap<String, List<Integer>>();
        metadataSelected.put("groups", data.getGroupsMetadata().stream().map(Catalog::getId).collect(Collectors.toList()));
        metadataSelected.put("tags", data.getTagsMetadata().stream().map(Catalog::getId).collect(Collectors.toList()));

        MediaUrlRecord mediaUrlRecord = new MediaUrlRecord();
        mediaUrlRecord.setUrl(data.getUrl());

        //handle view
        ModelAndView modelAndView = new ModelAndView("media/info");
        modelAndView.addObject("data", data);
        modelAndView.addObject("metadata", metadata);
        modelAndView.addObject("metadataClipboard", metadataClipboard);
        modelAndView.addObject("metadataSelected", metadataSelected);
        modelAndView.addObject("tab", tab);
        modelAndView.addObject("idCollection", globals.getIdCollection());
        modelAndView.addObject("activeMenu", "statistics");
        modelAndView.addObject("mediaUrlRecord", mediaUrlRecord);

        return modelAndView;
    }

    @GetMapping("/media/{idMedia}/open")
    @ResponseBody
    public void open(@PathVariable(name = "idMedia") Integer idMedia) {
        MediaInfo mediaInfo = mediaService.getInfo(idMedia);
        mediaService.open(mediaInfo.getLocation() + File.separator + mediaInfo.getName());

        Map<String, Object> params = new HashMap<>();
        params.put("last_viewed", System.currentTimeMillis() / 1000);
        params.put("view_count", mediaInfo.getViewCount() + 1);
        mediaService.update(idMedia, params);
    }

    @PostMapping("/media/{idMedia}/{type}/select")
    @ResponseBody
    public ModelAndView select(@PathVariable(name = "idMedia") Integer idMedia, @PathVariable(name = "type") String type, @RequestParam("ids") List<Integer> ids) {
        //delete current metadata for media
        mediaMetadataService.deleteByIdMediaAndType(idMedia, type);

        //insert relations
        for (Integer idMetadata: ids) {
            mediaMetadataService.insert(idMedia, idMetadata);
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/media/" + idMedia + "/info?tab=" + type);
        return modelAndView;
    }

    @PostMapping("/media/{idMedia}/{type}/type")
    @ResponseBody
    public ModelAndView type(@PathVariable(name = "idMedia") Integer idMedia, @PathVariable(name = "type") String type, @RequestParam("metadata") List<String> metadata) {
        //delete current metadata for media
        mediaMetadataService.deleteByIdMediaAndType(idMedia, type);

        //insert typed metadata if needed
        for (String row : metadata) {
            metadataService.insertIfNeeded(row, type);
        }

        //insert relations
        mediaMetadataService.inserts(idMedia, type, metadata);

        ModelAndView modelAndView = new ModelAndView("redirect:/media/" + idMedia + "/info?tab=" + type);
        return modelAndView;
    }

    @PostMapping("/media/{idMedia}/rating")
    @ResponseBody
    public void rating(@PathVariable(name = "idMedia") Integer idMedia, @RequestBody MediaRatingRecord record) {
        Map<String, Object> params = new HashMap<>();
        params.put("rating", record.getRating());
        mediaService.update(idMedia, params);
    }

    @GetMapping("/media/{idMedia}/unscrap")
    @ResponseBody
    public ModelAndView unscrapMedia(@PathVariable(name = "idMedia") Integer idMedia) {
        mediaService.unscrapMedia(idMedia, false);

        ModelAndView modelAndView = new ModelAndView("redirect:/media/" + idMedia + "/info?tab=");
        return modelAndView;
    }

    @GetMapping("/images")
    public ResponseEntity<Resource> serveImage(@RequestParam(name = "idCollection", required = true) Integer idCollection,
                                               @RequestParam(name = "idMedia", required = true) Integer idMedia) {

        Path thumbnailPath = locationService.getAppDataThumbnailMediaPath(idCollection, idMedia);

        Resource resource = mediaService.getResource(thumbnailPath);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping("/covers")
    public ResponseEntity<Resource> serveCover(@RequestParam(name = "idCollection", required = true) Integer idCollection,
                                               @RequestParam(name = "idMedia", required = true) Integer idMedia) {

        Path coverPath = locationService.getAppDataCoverMediaPath(idCollection, idMedia);

        Resource resource = mediaService.getResource(coverPath);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

     @GetMapping("/previews")
    public ResponseEntity<Resource> servePreview(@RequestParam(name = "idCollection", required = true) Integer idCollection,
                                               @RequestParam(name = "idMedia", required = true) Integer idMedia) {

        Path previewPath = locationService.getAppDataPreviewMediaPath(idCollection, idMedia);

        Resource resource = mediaService.getResource(previewPath);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping("/media/{idMedia}/url")
    public ModelAndView saveUrl(@PathVariable(name = "idMedia") Integer idMedia, MediaUrlRecord record) {
        mediaService.saveUrl(idMedia, record.getUrl());

        ModelAndView modelAndView = new ModelAndView("redirect:/media/" + idMedia + "/info?tab=");
        return modelAndView;
    }

    //api
    @GetMapping(path = {"/api/media/statistics"})
    @ResponseBody
    public List<MediaStatistic> apiMediaStatistics() {
        return mediaService.getStatistics();
    }

    @GetMapping(path = {"/api/media/{idMedia}/info"})
    @ResponseBody
    public MediaInfo apiMediaInfo(@PathVariable(name = "idMedia") Integer idMedia) {
        return mediaService.getInfo(idMedia);
    }

}