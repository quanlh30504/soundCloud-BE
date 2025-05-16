package com.example.soundCloud_BE.zingMp3;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ParseService {
    /**
     * Phân tích nội dung .lrc thành danh sách lời bài hát
     * @param lrcContent Nội dung tệp .lrc
     * @return Danh sách các dòng lời bài hát
     */
    public static List<Map<String, String>> parseLrcContent(String lrcContent) {

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
            lyrics.sort(Comparator.comparing(ParseService::parseTimestampToSeconds));
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
    public static boolean isValidTimestamp(String timestamp) {
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
    public static double parseTimestampToSeconds(LyricLine line) {
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
