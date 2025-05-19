package com.example.soundCloud_BE.zingMp3;

import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.repository.TrackRepository;
import com.example.soundCloud_BE.zingMp3.Dto.*;
import com.example.soundCloud_BE.zingMp3.Dto.Chart.ChartHomeData;
import com.example.soundCloud_BE.zingMp3.Dto.HomeData.HubDetail;
import com.example.soundCloud_BE.zingMp3.Dto.HomeData.RecommendSongs;
import com.example.soundCloud_BE.zingMp3.Dto.HomeData.Top100;
import jakarta.transaction.Transactional;
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

import javax.sound.midi.Track;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private TrackRepository trackRepository;


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


    @Transactional
    public SyncResponse syncSongToDatabase(String songId) {
        if (songId == null || songId.trim().isEmpty()) {
            log.error("songId is null or empty");
            return SyncResponse.failure("Invalid songId");
        }

        Optional<Tracks> optionalTrack = trackRepository.findBySpotifyId(songId);
        StreamData streamData = null;
        Tracks track;

        if (optionalTrack.isPresent()) {
            log.info("Bài hát đã tồn tại trong database, kiểm tra và cập nhật...");
            track = optionalTrack.get();

            if (track.getStreamUrl128() != null && !track.getStreamUrl128().isEmpty()) {
                streamData = getStreamUrl(songId);
                track.setStreamUrl128(streamData.get_128());
                track.setUpdatedAt(LocalDateTime.now());
                trackRepository.save(track);
            }
//            streamData = getStreamDataIfNeeded(track, streamData);
//            updateStreamUrls(track, streamData);
//            track.setUpdatedAt(LocalDateTime.now());
//            trackRepository.save(track);

        } else {
            log.info("Bài hát chưa tồn tại trong database, thêm mới...");
            SongData songData = getSongInfo(songId);
            if (songData == null) {
                log.error("Không tìm thấy thông tin bài hát với ID: {}", songId);
                return SyncResponse.failure("Song info not found");
            }

            streamData = getStreamUrl(songId);
            track = Tracks.builder()
                    .spotifyId(songId)
                    .title(songData.getTitle())
                    .artists(songData.getArtistsNames())
                    .coverUrl(songData.getThumbnail())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            updateStreamUrls(track, streamData);
            trackRepository.save(track);
        }

        return SyncResponse.success(songId);
    }

    private StreamData getStreamDataIfNeeded(Tracks track, StreamData streamData) {
        if (streamData == null && (isStreamUrlEmpty(track.getStreamUrl128()) || isStreamUrlEmpty(track.getStreamUrl320()))) {
            return getStreamUrl(track.getSpotifyId());
        }
        return streamData;
    }

    private void updateStreamUrls(Tracks track, StreamData streamData) {
        if (isStreamUrlEmpty(track.getStreamUrl128()) && streamData != null) {
            String url128 = streamData.get_128();
            if (url128 != null && !url128.isEmpty() && !url128.equals("VIP")) {
                track.setStreamUrl128(url128);
            } else {
                log.error("Không tìm thấy streamUrl128 cho bài hát với ID: {}", track.getSpotifyId());
            }
        }

        if (isStreamUrlEmpty(track.getStreamUrl320()) && streamData != null) {
            String url320 = streamData.get_320();
            if (url320 != null && !url320.isEmpty() && !url320.equals("VIP")) {
                track.setStreamUrl320(url320); // Sửa lỗi gán sai cột
            } else {
                log.error("Không tìm thấy streamUrl320 cho bài hát với ID: {}", track.getSpotifyId());
            }
        }
    }

    private boolean isStreamUrlEmpty(String url) {
        return url == null || url.trim().isEmpty();
    }


    @Transactional
    public SyncResponse syncSongToDatabaseWithBody(String songId, SongData songData) {
        if (songId == null || songId.trim().isEmpty()) {
            log.error("songId is null or empty");
            return SyncResponse.failure("Invalid songId");
        }

        Optional<Tracks> optionalTrack = trackRepository.findBySpotifyId(songId);
        Tracks track;

        if (!optionalTrack.isPresent()) {
            log.info("Bài hát chưa tồn tại trong database, thêm mới...");
            if (songData == null) {
                log.error("Không tìm thấy thông tin bài hát với ID: {}", songId);
                return SyncResponse.failure("Song info not found");
            }

            track = Tracks.builder()
                    .spotifyId(songId)
                    .title(songData.getTitle())
                    .artists(songData.getArtistsNames())
                    .coverUrl(songData.getThumbnail())
                    .duration(songData.getDuration())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            trackRepository.save(track);
        }

        return SyncResponse.success(songId);
    }


    // Các hàm lấy thông tin trang home

    // Get top100
    public List<Top100> getTop100() {
        String path = "/api/v2/page/get/top-100";
        String sig = hashService.hashParamNoId(path);

        Map<String, String> params = new HashMap<>();
        params.put("sig", sig);

        ApiResponse<List<Top100>> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<List<Top100>>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get home data: {}", errorMsg);
            throw new RuntimeException("Failed to get home data: " + errorMsg);
        }

        List<Top100> data = response.getData();
        if (data == null) {
            log.error("No home data available");
            throw new RuntimeException("No home data available");
        }

        return data;
    }


    //Get hub-detail . Ở đây lấy hub chill thư giãn (id hub: IWZ9Z0CI)
    public <T> HubDetail<T> getHubDetail (String hubId) {
        String path = "/api/v2/page/get/hub-detail";
        String sig = hashService.hashParamWithId(path, hubId);

        Map<String, String> params = new HashMap<>();
        params.put("id", hubId);
        params.put("sig", sig);

        ApiResponse<HubDetail<T>> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<HubDetail<T>>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get home data: {}", errorMsg);
            throw new RuntimeException("Failed to get home data: " + errorMsg);
        }

        HubDetail<T> data = response.getData();
        if (data == null) {
            log.error("No home data available");
            throw new RuntimeException("No home data available");
        }

        return data;
    }

    //Get recommend songs
    public List<SongData> getRecommendSongs() {
        String path = "/api/v2/song/get/section-song-station";
        String sig = hashService.hashParamWithCount(path, "20");

        Map<String, String> params = new HashMap<>();
        params.put("count", "20");
        params.put("sig", sig);

        ApiResponse<RecommendSongs> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<RecommendSongs>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get home data: {}", errorMsg);
            throw new RuntimeException("Failed to get home data: " + errorMsg);
        }

        List<SongData> data = response.getData().getItems();
        if (data == null) {
            log.error("No home data available");
            throw new RuntimeException("No home data available");
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    public List<?> getNewRelease(String type) {
        String path = "/api/v2/chart/get/new-release";
        String sig = hashService.hashParamWithType(path, type);

        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("sig", sig);

        // Xử lý theo từng type
        if ("song".equalsIgnoreCase(type)) {
            ApiResponse<List<SongData>> response = requestZingMp3(
                    path,
                    params,
                    new ParameterizedTypeReference<ApiResponse<List<SongData>>>() {}
            );

            if (response == null || response.getErr() != 0) {
                String errorMsg = response != null ? response.getMsg() : "No response";
                log.error("Failed to get new release songs: {}", errorMsg);
                throw new RuntimeException("Failed to get new release songs: " + errorMsg);
            }

            List<SongData> data = response.getData();
            if (data == null) {
                log.error("No new release song data available");
                throw new RuntimeException("No new release song data available");
            }

            return data;

        } else if ("album".equalsIgnoreCase(type)) {
            ApiResponse<List<Album>> response = requestZingMp3(
                    path,
                    params,
                    new ParameterizedTypeReference<ApiResponse<List<Album>>>() {}
            );

            if (response == null || response.getErr() != 0) {
                String errorMsg = response != null ? response.getMsg() : "No response";
                log.error("Failed to get new release albums: {}", errorMsg);
                throw new RuntimeException("Failed to get new release albums: " + errorMsg);
            }

            List<Album> data = response.getData();
            if (data == null) {
                log.error("No new release album data available");
                throw new RuntimeException("No new release album data available");
            }

            return data;

        } else {
            log.error("Invalid type: {}. Must be 'song' or 'album'", type);
            throw new IllegalArgumentException("Invalid type: " + type + ". Must be 'song' or 'album'");
        }
    }

    // BXH nhạc mới
    public Top100 getNewReleaseChart(){
        String path = "/api/v2/page/get/newrelease-chart";
        String sig = hashService.hashParamNoId(path);

        Map<String, String> params = new HashMap<>();
        params.put("sig", sig);

        ApiResponse<Top100> response = requestZingMp3(
                path,
                params,
                new ParameterizedTypeReference<ApiResponse<Top100>>() {}
        );

        if (response == null || response.getErr() != 0) {
            String errorMsg = response != null ? response.getMsg() : "No response";
            log.error("Failed to get new release chart: {}", errorMsg);
            throw new RuntimeException("Failed to get new release chart: " + errorMsg);
        }

        Top100 data = response.getData();
        if (data == null) {
            log.error("No new release chart data available");
            throw new RuntimeException("No new release chart data available");
        }

        return data;
    }























}
