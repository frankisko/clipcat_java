package com.frankisko.clipcat.services;


import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dao.MediaMapper;
import com.frankisko.clipcat.dto.MediaDB;
import com.frankisko.clipcat.dto.MediaInfo;
import com.frankisko.clipcat.dto.MediaRatingRecord;
import com.frankisko.clipcat.dto.MediaStatistic;
import com.frankisko.clipcat.dto.MediaToScrap;
import com.frankisko.clipcat.dto.GalleryMedia;
import com.frankisko.clipcat.entities.Media;

@Service
public class MediaService {
    @Value("${viewer}")
    private String viewer;

    @Value("${data_directory}")
    private String dataDirectory;

    @Value("${ffmpeg}")
    private String ffmpeg;

    private final LocationService locationService;

    private final MetadataService metadataService;

    private final MediaMetadataService mediaMetadataService;

    private final MediaMapper mediaMapper;

    private final Globals globals;

    public MediaService(LocationService locationService, MetadataService metadataService,
                        MediaMetadataService mediaMetadataService,
                        MediaMapper mediaMapper, Globals globals) {
        this.locationService = locationService;
        this.metadataService = metadataService;
        this.mediaMetadataService = mediaMetadataService;
        this.mediaMapper = mediaMapper;
        this.globals = globals;
    }

    public List<MediaStatistic> getStatistics() {
        return mediaMapper.getStatistics(globals.getIdCollection());
    }

    public MediaInfo getInfo(Integer idMedia) {
        return mediaMapper.getInfo(idMedia);
    }

    public void open(String name) {
        try {
            //open media
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = new ArrayList<>();
            command.add(viewer);
            command.add(name);
            processBuilder.command(command);

            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Integer idMedia, Map<String, Object> params) {
        mediaMapper.update(idMedia, params);
    }

    public List<GalleryMedia> getGalleryMedia(Map<String, Object> filters) {
        return mediaMapper.getGalleryMedia(globals.getIdCollection(), filters);
    }

    public List<MediaDB> getMediaDB() {
        return mediaMapper.getMediaDB(globals.getIdCollection());
    }

    public void scrapMediaBulk(List<Integer> idsMedia) {
        mediaMapper.scrapMediaBulk(idsMedia);
    }

    public void unscrapMedia(Integer idMedia, Boolean keepCover) {
        this.deleteThumbnail(idMedia);
        this.deletePreview(idMedia);

        if (!keepCover) {
            this.deleteCover(idMedia);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("scrapped", 0);
        params.put("has_cover", keepCover? 1 : 0);
        mediaMapper.update(idMedia, params);
    }

    public void unscrapAll() {
        mediaMapper.unscrapAll(globals.getIdCollection());
    }

    public void saveUrl(Integer idMedia, String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        mediaMapper.update(idMedia, params);
    }

    public void delete(Integer idMedia) {
        mediaMetadataService.deleteByIdMedia(idMedia);
        mediaMapper.delete(idMedia);
    }

    public void deleteByIdCollection(Integer idCollection) {
        mediaMapper.deleteByIdCollection(idCollection);
    }

    public void deleteThumbnail(Integer idMedia) {
        Path thumbnailMediaPath = locationService.getAppDataThumbnailMediaPath(globals.getIdCollection(), idMedia);
        File file = thumbnailMediaPath.toFile();
        file.delete();
    }

    public void deleteCover(Integer idMedia) {
        Path coverMediaPath = locationService.getAppDataCoverMediaPath(globals.getIdCollection(), idMedia);
        File file = coverMediaPath.toFile();
        file.delete();
    }

    public void deletePreview(Integer idMedia) {
        Path previewMediaPath = locationService.getAppDataPreviewMediaPath(globals.getIdCollection(), idMedia);
        File file = previewMediaPath.toFile();
        file.delete();
    }

    public Resource getResource(Path resourcePath) {
        Resource resource = null;

        // Serve the image as a resource
        try {
            resource = new UrlResource(resourcePath.toUri());
        } catch (Exception e) {

        }
        return resource;

    }

    public void insert(Media media) {
        mediaMapper.insert(media);
    }

    public Integer countMediaInCollection(Integer idCollection) {
        return mediaMapper.countMediaInCollection(idCollection);
    }

    public Integer countPendingMediaInCollection(Integer idCollection) {
        return mediaMapper.countPendingMediaInCollection(idCollection);
    }

    public MediaToScrap getMediaToScrap(Integer idCollection) {
        return mediaMapper.getMediaToScrap(idCollection);
    }

    public String processMedia(Integer idCollection) {
        String response = "";

        MediaToScrap mediaToScrap = this.getMediaToScrap(idCollection);

        Long duration = 0l;

        if (mediaToScrap != null) {
            response = mediaToScrap.getName();

            Path mediaToScrapPath = Paths.get(mediaToScrap.getLocation(), File.separator, mediaToScrap.getName());
            String mediaToScrapName = mediaToScrapPath.toString();

            if (isValidExtension(mediaToScrapName)) {
                duration = getVideoDuration(mediaToScrapName);
            }

            createThumbnail(idCollection, mediaToScrap.getIdMedia(), mediaToScrapPath.toFile(), Math.floor(duration / 10));

            generatePreview(idCollection, mediaToScrap.getIdMedia(), mediaToScrapPath.toFile(), duration);

            File file = mediaToScrapPath.toFile();

            Map<String, Object> params = new HashMap<>();
            params.put("size", file.length());
            params.put("duration", duration);
            params.put("scrapped", 1);
            this.update(mediaToScrap.getIdMedia(), params);
        }

        return response;
    }

    private String getExtension(String filename) {
        String extension = "";

        int i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i);
        }

        return extension;
    }

    public boolean isValidExtension(String filename) {
        filename = filename.toLowerCase();

        if (filename.endsWith(".mp4") || filename.endsWith(".flv") || filename.endsWith(".avi") || filename.endsWith(".wmv") ||
            filename.endsWith(".mpeg") || filename.endsWith(".mpg") || filename.endsWith(".mkv") || filename.endsWith(".m4v") || filename.endsWith(".webm")) {
            return true;
        }

        return false;
    }

    public Long getVideoDuration(String video) {
         try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", video);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Duration:")) {
                    // Parse the duration line to extract the duration in HH:MM:SS format
                    String durationLine = line.trim().split(",")[0].split(" ")[1];
                    String[] durationParts = durationLine.split(":");
                    int hours = Integer.parseInt(durationParts[0]);
                    int minutes = Integer.parseInt(durationParts[1]);
                    double seconds = Double.parseDouble(durationParts[2]);
                    return (long) ((hours * 3600) + (minutes * 60) + seconds);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0l;  // Return -1 if unable to determine the duration
    }

    private void createThumbnail(Integer idCollection, Integer idMedia, File file, Double seekLength) {
       try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpeg,
                "-ss", seekLength.toString(),
                "-i", '"' + file.toString() + '"',
                "-vf", "scale=350:200:force_original_aspect_ratio=decrease,pad=350:200:(ow-iw)/2:(oh-ih)/2",
                "-y",
                "-nostats",
                "-loglevel", "0",
                locationService.getAppDataThumbnailMediaPath(idCollection, idMedia).toString()
             );
            Process process = processBuilder.start();
            System.out.println(process.toString());
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thumbnail generation interrupted: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generatePreview(Integer idCollection, Integer idMedia, File file, Long duration) {

        List<String> command = new ArrayList<>();
        command.add(ffmpeg);

        command.add("-i");
        command.add('"' + file.toString() + '"');

        command.add("-y");
        command.add("-filter_complex");

        Integer segmentDuration = 1;
        Integer segmentsChunks = 20;
        //if file is too short, set less chunks
        if (duration < 30) {
            segmentsChunks = 5;
        }

        Double seekLength = Math.floor(duration / segmentsChunks);

        String subcommand = "";

        for (int i = 0; i < segmentsChunks; i++) {
            Double seeking = 0 + (seekLength * i);

            subcommand += "[0:v]trim=start=" + seeking.toString() +
                            ":duration=" + segmentDuration.toString() +
                            ",setpts=PTS-STARTPTS,setpts=0.5*PTS,scale=w=350:h=200:force_original_aspect_ratio=decrease,pad=350:200:(ow-iw)/2:(oh-ih)/2[v" + String.valueOf(i) + "];";
        }

        for (int i = 0; i < segmentsChunks; i++) {
            subcommand += "[v" + String.valueOf(i) + "]";
        }

        subcommand += "concat=n=20:v=1:a=0[out]";

        command.add('"' + subcommand + '"');
        command.add("-map");
        command.add("[out]");
        command.add(locationService.getAppDataPreviewMediaPath(idCollection, idMedia).toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        String commandPreview = processBuilder.command().stream().collect(Collectors.joining(" "));

        System.out.println(commandPreview);

        try {
            Process process = processBuilder.start();

            // Consume the input stream to prevent blocking
            try (InputStream inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //System.out.println(line);
                }
            }

            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
