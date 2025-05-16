package com.example.soundCloud_BE.zingMp3;

import com.example.soundCloud_BE.zingMp3.Dto.*;
import com.example.soundCloud_BE.zingMp3.Dto.Chart.ChartHomeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ZingMp3ApiService {

    @Value("${zing-mp3.BASE_URL}")
    private String BASE_URL;
    @Value("${zing-mp3.API_KEY}")
    private String API_KEY;
    @Value("${zing-mp3.SECRET_KEY}")
    private String SECRET_KEY;
    @Value("${zing-mp3.VERSION}")
    private String VERSION;
    @Value("${zing-mp3.COOKIE_PATH}")
    private String COOKIE_PATH;
    @Autowired
    private HashService hashService;

    @Autowired
    private RestTemplate restTemplate;

    public String getCookie() {
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                String.class
        );

        HttpHeaders headers = response.getHeaders();
        List<String> cookies = headers.get("Set-Cookie");

        if (cookies != null && cookies.size() > 1) {
            return cookies.get(1);
        }
        throw new RuntimeException("Không tìm thấy cookie.");
    }

    public <T> ApiResponse<T> requestZingMp3(String path, Map<String, String> params, ParameterizedTypeReference<ApiResponse<T>> responseType) {
        String cookie = getCookie();

        // Xây dựng URL với các query params
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL + path)
                .queryParam("ctime", hashService.getCTIME())
                .queryParam("version", VERSION)
                .queryParam("apiKey", API_KEY);

        params.forEach(builder::queryParam);

        // Thiết lập headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<ApiResponse<T>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Zing MP3 API request failed with status: " + response.getStatusCode());
            }

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Zing MP3 API error: " + e.getMessage(), e);
        }
    }


    // Lấy streamUrl của bài hát
    public StreamData getStreamUrl(String songId) {
        String path = "/api/v2/song/get/streaming";
        String sig = hashService.hashParamWithId(path, songId);

        Map<String, String> params = new HashMap<>();
        params.put("id", songId);
        params.put("sig", sig);

        ApiResponse<StreamData> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<StreamData>>() {});

        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }

        StreamData data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }

        return data;
    }

    // Lấy thông tin bài hát
    public SongData getSongInfo(String songId) {
        String path = "/api/v2/song/get/info";
        String sig = hashService.hashParamWithId(path, songId);

        Map<String, String> params = new HashMap<>();
        params.put("id", songId);
        params.put("sig", sig);


        ApiResponse<SongData> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<SongData>>() {});


        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }


        SongData data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }

        return data;
    }


    // Lấy lời bài hát
    public List<Map<String, String>> getLyrics(String songId) {
        String path = "/api/v2/lyric/get/lyric";
        String sig = hashService.hashParamWithId(path, songId);

        Map<String, String> params = new HashMap<>();
        params.put("id", songId);
        params.put("sig", sig);

        ApiResponse<Lyric> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<Lyric>>() {});

        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }

        Lyric data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }
        String lrcUrl = data.getFile();
        // Tải nội dung .lrc
        ResponseEntity<String> lrcResponse = restTemplate.getForEntity(lrcUrl, String.class);
        if (!lrcResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to fetch .lrc file for song {}: {}", songId, lrcResponse.getStatusCode());
            return null;
        }

        // Phân tích nội dung .lrc
        String lrcContent = lrcResponse.getBody();
        List<Map<String, String>> lyrics = ParseService.parseLrcContent(lrcContent);

        return lyrics;

    }

    //Search multi
    public SearchMultiResponse searchMulti(String query){
        if (query == null || query.trim().isEmpty()) {
            log.error("Query cannot be null or empty");
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        // Chuẩn hóa query: mã hóa URL
        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        log.info("Original query: {}, Encoded query: {}", query, encodedQuery);

        String path = "/api/v2/search/multi";
        String sig = hashService.hashParamNoId(path);
        Map<String, String> params = new HashMap<>();
        params.put("q", encodedQuery);
        params.put("sig", sig);

        ApiResponse<SearchMultiResponse> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<SearchMultiResponse>>() {});

        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }

        SearchMultiResponse data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }

        return data;

    }

    //Lấy thông tin của artist - sử dụng alias của nghệ sĩ
    public Artist getArtistInfo(String alias) {
        String path = "/api/v2/page/get/artist";
        String sig = hashService.hashParamNoId(path);

        Map<String, String> params = new HashMap<>();
        params.put("alias", alias);
        params.put("sig", sig);

        ApiResponse<Artist> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<Artist>>() {});

        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }

        Artist data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }

        return data;
    }

    // Lấy danh sách bài hát của một nghệ sĩ
    public Items<SongData> getListArtistSong(String artistId, String page, String count) {

        if (artistId == null || artistId.trim().isEmpty()) {
            log.error("artistId cannot be null or empty");
            throw new IllegalArgumentException("artistId cannot be null or empty");
        }
        if (page == null || page.trim().isEmpty()) {
            log.error("page cannot be null or empty");
            throw new IllegalArgumentException("page cannot be null or empty");
        }
        if (count == null || count.trim().isEmpty()) {
            log.error("count cannot be null or empty");
            throw new IllegalArgumentException("count cannot be null or empty");
        }

        String path = "/api/v2/song/get/list";
        String sig = hashService.hashListMV(path, artistId, "artist", page, count);

        Map<String, String> params = new HashMap<>();
        params.put("id", artistId);
        params.put("type", "artist");
        params.put("page", page);
        params.put("count", count);
        params.put("sort", "new");
        params.put("sectionId", "aSong");
        params.put("sig", sig);

        log.info("Fetching artist songs for artistId: {}, params: {}", artistId, params);

        ApiResponse<Items<SongData>> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<Items<SongData>>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get artist songs for artistId {}: {}", artistId, errorMsg);
            throw new RuntimeException("Failed to get artist songs: " + errorMsg);
        }

        Items data = response.getData();
        if (data == null) {
            log.error("No artist songs data available for artistId {}", artistId);
            throw new RuntimeException("No artist songs data available");
        }

        data.setPage(page);
        data.setCount(count);
        return data;
    }

    // Lấy danh sách album/playlist của một nghệ sĩ
    public Items<Album> getListArtistPlaylist(String artistId, String page, String count) {

        if (artistId == null || artistId.trim().isEmpty()) {
            log.error("artistId cannot be null or empty");
            throw new IllegalArgumentException("artistId cannot be null or empty");
        }
        if (page == null || page.trim().isEmpty()) {
            log.error("page cannot be null or empty");
            throw new IllegalArgumentException("page cannot be null or empty");
        }
        if (count == null || count.trim().isEmpty()) {
            log.error("count cannot be null or empty");
            throw new IllegalArgumentException("count cannot be null or empty");
        }

        String path = "/api/v2/playlist/get/list";
        String sig = hashService.hashListMV(path, artistId, "artist", page, count);

        Map<String, String> params = new HashMap<>();
        params.put("id", artistId);
        params.put("type", "artist");
        params.put("page", page);
        params.put("count", count);
        params.put("sort", "new");
        params.put("sectionId", "aPlaylist");
        params.put("sig", sig);

        log.info("Fetching artist playlists for artistId: {}, params: {}", artistId, params);

        ApiResponse<Items<Album>> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<Items<Album>>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get artist playlists for artistId {}: {}", artistId, errorMsg);
            throw new RuntimeException("Failed to get artist playlists: " + errorMsg);
        }

        Items<Album> data = response.getData();
        if (data == null) {
            log.error("No artist playlists data available for artistId {}", artistId);
            throw new RuntimeException("No artist playlists data available");
        }
        data.setPage(page);
        data.setCount(count);

        return data;
    }

    // Lấy thông tin album/playlist
    public Album getPlaylistInfo(String playlistId) {
        String path = "/api/v2/page/get/playlist";

        String sig = hashService.hashParamWithId(path, playlistId);

        Map<String, String> params = new HashMap<>();
        params.put("id", playlistId);
        params.put("sig", sig);

        ApiResponse<Album> response = requestZingMp3(path, params, new ParameterizedTypeReference<ApiResponse<Album>>() {});

        if (response == null || response.getErr() != 0) {
            throw new RuntimeException("Failed to get stream data: " +
                    (response != null ? response.getMsg() : "No response"));
        }

        Album data = response.getData();
        if (data == null) {
            throw new RuntimeException("No stream data available");
        }

        return data;
    }


    // Lấy thông tin chart home
    public ChartHomeData getChartHome() {

        String path = "/api/v2/page/get/chart-home";
        String sig = hashService.hashParamNoId(path);

        Map<String, String> params = new HashMap<>();
        params.put("sig", sig);


        ApiResponse<ChartHomeData> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<ChartHomeData>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get chart home: {}", errorMsg);
            throw new RuntimeException("Failed to get chart home: " + errorMsg);
        }

        ChartHomeData data = response.getData();
        if (data == null) {
            log.error("No chart home data available");
            throw new RuntimeException("No chart home data available");
        }

        return data;
    }


























}
