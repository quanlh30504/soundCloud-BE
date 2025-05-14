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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ZingMp3Service {
    @Value("${zingmp3.server.url:http://localhost:8088}")
    private String zingMp3ServerUrl;

    @Autowired
    private RestTemplate restTemplate;


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
    public String getStreamingUrl(String zingId) {
        try {
            String url = zingMp3ServerUrl + "/api/song/streamUrl/" + zingId;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("data")) {
                throw new RuntimeException("Invalid response from Zing MP3 server");
            }

            JsonNode data = response.get("data");

            String streamUrls = null;
            
            if (data.has("128")) {
                streamUrls = data.get("128").asText();
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
     * Phân tích nội dung .lrc thành danh sách lời bài hát
     * @param lrcContent Nội dung tệp .lrc
     * @return Danh sách các dòng lời bài hát
     */
    private List<Map<String, String>> parseLrcContent(String lrcContent) {

        if (lrcContent == null || lrcContent.trim().isEmpty()) {
            log.warn("LRC content is null or empty");
            return Collections.emptyList();
        }

        List<LyricLine> lyrics = new ArrayList<>();

        // Regex khớp timestamp: [mm:ss.xx] hoặc [mm:ss.xxx]
        String timestampRegex = "\\[(\\d{1,2}:\\d{2}\\.\\d{2,3})\\]";
        Pattern timestampPattern = Pattern.compile(timestampRegex, Pattern.UNICODE_CHARACTER_CLASS);

        String[] lines = lrcContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue; // Bỏ qua dòng rỗng
            }

            // Bỏ qua metadata như [ar:Artist], [ti:Title]
            if (line.matches("\\[\\w+:.+\\]")) {
                log.debug("Skipping metadata: {}", line);
                continue;
            }

            // Xử lý dòng có timestamp
            Matcher matcher = timestampPattern.matcher(line);
            if (!matcher.find()) {
                log.debug("Skipping line without valid timestamp: {}", line);
                continue;
            }

            String timestamp = matcher.group(1); // Lấy mm:ss.xx hoặc mm:ss.xxx

            // Kiểm tra timestamp hợp lệ
            if (!isValidTimestamp(timestamp)) {
                log.warn("Invalid timestamp: {}", timestamp);
                continue;
            }

            // Lấy text sau timestamp (bao gồm trường hợp text rỗng)
            String text = "";
            if (matcher.end() < line.length()) {
                text = line.substring(matcher.end()).trim();
            } else {
                log.debug("Found timestamp-only line: {}", timestamp);
            }

            lyrics.add(new LyricLine(timestamp, text));
        }

        // Sắp xếp lyrics theo timestamp
        if (!lyrics.isEmpty()) {
            lyrics.sort(Comparator.comparing(this::parseTimestampToSeconds));
        } else {
            log.warn("No valid lyrics found in LRC content");
        }

        // Chuyển sang List<Map<String, String>> để giữ tương thích
        return lyrics.stream()
                .map(LyricLine::toMap)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra timestamp hợp lệ (mm:ss.xx hoặc mm:ss.xxx)
     */
    private boolean isValidTimestamp(String timestamp) {
        try {
            String[] timeParts = timestamp.split("[:\\.]");
            if (timeParts.length != 3) {
                return false;
            }

            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);
            int milliseconds = Integer.parseInt(timeParts[2]);

            // Kiểm tra giới hạn
            if (minutes >= 60 || seconds >= 60) {
                return false;
            }

            // Kiểm tra độ dài mili giây (2 hoặc 3 chữ số)
            if (timeParts[2].length() != 2 && timeParts[2].length() != 3) {
                return false;
            }

            // Kiểm tra giá trị mili giây hợp lệ
            if (timeParts[2].length() == 2 && milliseconds > 99) {
                return false;
            }
            if (timeParts[2].length() == 3 && milliseconds > 999) {
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Chuyển timestamp thành giây để sắp xếp
     */
    private double parseTimestampToSeconds(LyricLine line) {
        try {
            String[] parts = line.timestamp().split("[:\\.]");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            int milliseconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            // Xử lý mili giây dựa trên độ dài (2 hoặc 3 chữ số)
            double msFactor = parts[2].length() == 2 ? 100.0 : 1000.0;
            return minutes * 60.0 + seconds + milliseconds / msFactor;
        } catch (Exception e) {
            log.warn("Error parsing timestamp {}: {}", line.timestamp(), e.getMessage());
            return 0.0;
        }
    }


}
