package com.example.soundCloud_BE.zingMp3;

import java.util.HashMap;
import java.util.Map;

/**
 * Record đại diện cho một dòng lời bài hát
 */
public record LyricLine(String timestamp, String text) {
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("text", text);
        return map;
    }
}