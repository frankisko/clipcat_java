package com.frankisko.clipcat.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.frankisko.clipcat.config.Globals;
import com.frankisko.clipcat.dto.CollectionRecord;
import com.frankisko.clipcat.dto.CollectionRow;
import com.frankisko.clipcat.dto.MediaInfo;
import com.frankisko.clipcat.dto.MediaRatingRecord;
import com.frankisko.clipcat.dto.MediaStatistic;
import com.frankisko.clipcat.dto.MetadataRecord;
import com.frankisko.clipcat.dto.MetadataRow;

@Service
public class ApiClientService {

    @Value("${api_url}")
    private String apiUrl;

    @Autowired
    private Globals globals;

    //collections
    public List<CollectionRow> collectionsAll() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<List<CollectionRow>> response = restTemplate.exchange(
            apiUrl + "/collections",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<CollectionRow>>() {
            }
        );
        List<CollectionRow> responseBody = response.getBody();

        return responseBody;
    }

    public void collectionInsert(CollectionRecord record) {
      RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<CollectionRecord> httpEntity = new HttpEntity<>(record, httpHeaders);

        restTemplate.exchange(
            apiUrl + "/collections",
            HttpMethod.POST,
            httpEntity,
            Void.class
        );
    }

    public void collectionUpdate(Integer idCollection, CollectionRecord record) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<CollectionRecord> httpEntity = new HttpEntity<>(record, httpHeaders);

        restTemplate.exchange(
            apiUrl + "/collections/{idCollection}",
            HttpMethod.PUT,
            httpEntity,
            Void.class,
            idCollection
        );
    }

    public void collectionDelete(Integer idCollection) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        restTemplate.exchange(
            apiUrl + "/collections/{idCollection}",
            HttpMethod.DELETE,
            httpEntity,
            Void.class,
            idCollection
        );
    }

    public CollectionRecord collectionById(Integer idCollection) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<CollectionRecord> response = restTemplate.exchange(
            apiUrl + "/collections/{idCollection}",
            HttpMethod.GET,
            httpEntity,
            CollectionRecord.class,
            idCollection
        );
        CollectionRecord record = response.getBody();

        return record;
    }

    //metadata
    public List<MetadataRow> metadataAllByType(String type) {
     RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<List<MetadataRow>> response = restTemplate.exchange(
            apiUrl + "/metadata?idCollection={idCollection}&type={type}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<MetadataRow>>() {
            },
            globals.getIdCollection(),
            type
        );
        List<MetadataRow> responseBody = response.getBody();

        return responseBody;
    }

    public void metadataInsert(MetadataRecord record) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<MetadataRecord> httpEntity = new HttpEntity<>(record, httpHeaders);

        restTemplate.exchange(
            apiUrl + "/metadata",
            HttpMethod.POST,
            httpEntity,
            Void.class
        );
    }

    public MetadataRecord metadataById(Integer idMetadata) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<MetadataRecord> response = restTemplate.exchange(
            apiUrl + "/metadata/{idMetadata}",
            HttpMethod.GET,
            httpEntity,
            MetadataRecord.class,
            idMetadata
        );

        MetadataRecord record = response.getBody();

        return record;
    }

    public void metadataUpdate(Integer idMetadata, MetadataRecord record) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<MetadataRecord> httpEntity = new HttpEntity<>(record, httpHeaders);

        restTemplate.exchange(
            apiUrl + "/metadata/{idMetadata}",
            HttpMethod.PUT,
            httpEntity,
            Void.class,
            idMetadata
        );
    }

    public void metadataDelete(Integer idMetadata) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        restTemplate.exchange(
            apiUrl + "/metadata/{idMetadata}",
            HttpMethod.DELETE,
            httpEntity,
            Void.class,
            idMetadata
        );
    }

    //media
    public List<MediaStatistic> mediaStatistics() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<List<MediaStatistic>> response = restTemplate.exchange(
            apiUrl + "/media/statistics?idCollection={idCollection}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<List<MediaStatistic>>() {
            },
            globals.getIdCollection()
        );
        List<MediaStatistic> responseBody = response.getBody();

        return responseBody;
    }

    public MediaInfo mediaInfo(Integer idMedia) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<MediaInfo> response = restTemplate.exchange(
            apiUrl + "/media/{idMedia}/info",
            HttpMethod.GET,
            httpEntity,
            MediaInfo.class,
            idMedia
        );
        MediaInfo responseBody = response.getBody();

        return responseBody;
    }

    public void rating(Integer idMedia, MediaRatingRecord record) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<MediaRatingRecord> httpEntity = new HttpEntity<>(record, httpHeaders);

        restTemplate.exchange(
            apiUrl + "/media/{idMedia}/rating",
            HttpMethod.POST,
            httpEntity,
            Void.class,
            idMedia
        );
    }

}