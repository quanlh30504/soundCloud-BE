package com.example.soundCloud_BE.zingMp3;

import com.example.soundCloud_BE.service.SpotifyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ZingMp3Service {
    @Value("${zingmp3.server.url:http://localhost:8088}")
    private String zingMp3ServerUrl;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private SpotifyService spotifyService;

    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Custom exception for Zing MP3 API errors.
     */
    public static class ZingMp3Exception extends RuntimeException {
        public ZingMp3Exception(String message) {
            super(message);
        }

        public ZingMp3Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Lấy URL streaming của bài hát
     * @param zingId ID bài hát trên Zing MP3
     * @return Map chứa các URL stream với quality khác nhau (128, 320)
     */
    public Map<String, String> getStreamingUrl(String zingId) {
        try {
            String url = zingMp3ServerUrl + "/api/song/streamUrl/" + zingId;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("data")) {
                throw new RuntimeException("Invalid response from Zing MP3 server");
            }

            JsonNode data = response.get("data");
            Map<String, String> streamUrls = new HashMap<>();
            
            if (data.has("128")) {
                streamUrls.put("128", data.get("128").asText());
            }
            if (data.has("320")) {
                streamUrls.put("320", data.get("320").asText());
            }
            
            return streamUrls;
        } catch (Exception e) {
            log.error("Error getting streaming URL for song {}: {}", zingId, e.getMessage());
            throw new RuntimeException("Failed to get streaming URL", e);
        }
    }


    /**
     * Tải nhạc từ URL stream
     * @param streamUrl URL stream của bài hát (ví dụ: từ getStreamingUrl)
     * @return Dữ liệu byte của tệp âm thanh hoặc null nếu không tải được
     * @throws ZingMp3Exception nếu có lỗi trong quá trình tải
     */
    public byte[] downloadSong(String streamUrl) {
        try {
            if (streamUrl == null || streamUrl.isEmpty()) {
                log.error("Invalid stream URL: {}", streamUrl);
                throw new ZingMp3Exception("Invalid stream URL");
            }

            log.debug("Downloading song from URL: {}", streamUrl);

            // Sử dụng URI để giữ nguyên URL
            URI uri = new URI(streamUrl);
            ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to download song from {}: HTTP {}", streamUrl, response.getStatusCode());
                throw new ZingMp3Exception("Failed to download song: HTTP " + response.getStatusCode());
            }

            byte[] audioData = response.getBody();
            if (audioData == null || audioData.length == 0) {
                log.error("Empty audio data from {}", streamUrl);
                throw new ZingMp3Exception("Empty audio data");
            }

            log.info("Successfully downloaded song from {} (size: {} bytes)", streamUrl, audioData.length);
            return audioData;

        } catch (ZingMp3Exception e) {
            throw e;
        } catch (Exception e) {
            log.error("Error downloading song from {}: {}", streamUrl, e.getMessage(), e);
            throw new ZingMp3Exception("Error downloading song from: " + streamUrl, e);
        }
    }

    /**
     * Lấy lyrics của bài hát
     * @param zingId ID bài hát trên Zing MP3
     * @return Danh sách các dòng lời bài hát kèm thời gian
     */
    public List<Map<String, String>> getLyrics(String zingId) {
        try {
            // Lấy URL từ server Node.js
            String url = zingMp3ServerUrl + "/api/song/lyric/" + zingId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get lyrics URL for song {}: {}", zingId, response.getStatusCode());
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode == null || !jsonNode.has("data") || !jsonNode.get("data").has("file")) {
                log.info("No lyrics found for song {}", zingId);
                return null;
            }

            // Lấy URL tệp .lrc
            String lrcUrl = jsonNode.get("data").get("file").asText("");
            log.debug("Fetching .lrc file from: {}", lrcUrl);

            // Tải nội dung .lrc
            ResponseEntity<String> lrcResponse = restTemplate.getForEntity(lrcUrl, String.class);
            if (!lrcResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to fetch .lrc file for song {}: {}", zingId, lrcResponse.getStatusCode());
                return null;
            }

            // Phân tích nội dung .lrc
            String lrcContent = lrcResponse.getBody();
            List<Map<String, String>> lyrics = parseLrcContent(lrcContent);
            return lyrics;

        } catch (Exception e) {
            log.error("Error getting lyrics for song {}: {}", zingId, e.getMessage());
            return null;
        }
    }

    /**
     * Phân tích nội dung .lrc thành danh sách các dòng lời bài hát
     */
    private List<Map<String, String>> parseLrcContent(String lrcContent) {
        List<Map<String, String>> lyrics = new ArrayList<>();
        String[] lines = lrcContent.split("\n");

        for (String line : lines) {
            // Bỏ qua các dòng metadata như [ar:Artist] hoặc rỗng
            if (!line.matches("\\[\\d{2}:\\d{2}\\.\\d{2}\\].*")) {
                continue;
            }

            // Tách timestamp và text
            String[] parts = line.split("]", 2);
            if (parts.length < 2) {
                continue;
            }

            String timestamp = parts[0].substring(1); // Bỏ dấu [
            String text = parts[1].trim();

            Map<String, String> lyricLine = new HashMap<>();
            lyricLine.put("timestamp", timestamp);
            lyricLine.put("text", text);
            lyrics.add(lyricLine);
        }

        return lyrics;
    }

    /**
            * Converts Spotify track ID to Zing MP3 song ID.
     *
             * @param spotifyId ID bài hát trên Spotify
     * @return Zing MP3 ID hoặc null nếu không tìm thấy
     */
//    @Cacheable(value = "zingMp3IdMapping", key = "#spotifyId")
    public String convertSpotifyIdToZingId(String spotifyId) {
        try {
            // Get track info from Spotify
            var spotifyTrack = spotifyService.getTrack(spotifyId);
            if (spotifyTrack == null || spotifyTrack.getArtists() == null || spotifyTrack.getArtists().isEmpty()) {
                log.warn("Spotify track not found or has no artists for ID: {}", spotifyId);
                return null;
            }

            var artistsName = spotifyTrack.getArtists().stream()
                    .collect(Collectors.joining(", "));

            // Create search query
            String searchQuery = spotifyTrack.getName() + " " + spotifyTrack.getArtists().get(0) ;
            log.debug("Search query for Spotify ID {}: {}", spotifyId, searchQuery);

            // Build URL
            String searchUrl = UriComponentsBuilder
                    .fromHttpUrl(zingMp3ServerUrl + "/api/search")
                    .queryParam("keyword", URLEncoder.encode(searchQuery, StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            // Send request
            log.info("Sending search request to: {}", searchUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);

            // Check HTTP status
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Search request failed with status: {}", response.getStatusCode());
                throw new ZingMp3Exception("Search request failed with status: " + response.getStatusCode());
            }

            // Parse response
            String responseBody = response.getBody();
            log.debug("Response body: {}", responseBody);
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Check for API error
            if (jsonNode.has("err") && jsonNode.get("err").asInt() != 0) {
                log.error("Zing MP3 API error: {}", jsonNode.get("msg").asText());
                throw new ZingMp3Exception("Zing MP3 API error: " + jsonNode.get("msg").asText());
            }

            // Extract data
            JsonNode data = jsonNode.has("data") ? jsonNode.get("data") : null;
            if (data == null || !data.has("songs") || !data.get("songs").isArray() || data.get("songs").isEmpty()) {
                log.info("No matching songs found for query: {}", searchQuery);
                return null;
            }

            // Return the ID of the first matching song
            String zingId = data.get("songs").get(0).get("encodeId").asText("");
            log.info("Converted Spotify ID {} to Zing MP3 ID: {}", spotifyId, zingId);
            return zingId;

        } catch (ZingMp3Exception e) {
            log.error("Error converting Spotify ID {}: {}", spotifyId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error converting Spotify ID {}: {}", spotifyId, e.getMessage(), e);
            throw new ZingMp3Exception("Error converting Spotify ID to Zing MP3 ID: " + spotifyId, e);
        }
    }
}
