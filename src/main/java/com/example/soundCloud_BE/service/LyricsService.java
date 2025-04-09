package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.api.LyricsOvhApi;
import com.example.soundCloud_BE.dto.LyricsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LyricsService {

    private final LyricsOvhApi lyricsOvhApi;

    public LyricsResponse getLyrics(String trackName, String artistName) {
        try {
            // Clean and normalize track and artist names
            String cleanTrackName = cleanText(trackName);
            String cleanArtistName = cleanText(artistName);
            
            log.info("Searching lyrics for track: '{}' by artist: '{}'", cleanTrackName, cleanArtistName);
            
            Call<LyricsResponse> call = lyricsOvhApi.getLyrics(cleanArtistName, cleanTrackName);
            Response<LyricsResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                LyricsResponse lyrics = response.body();
                // Set additional fields for our application
                lyrics.setTrackName(trackName);
                lyrics.setArtistName(artistName);
                return lyrics;
            } else {
                log.warn("Failed to fetch lyrics: {} - {}", response.code(), response.message());
                
                String errorMessage = "";
                if (response.errorBody() != null) {
                    errorMessage = response.errorBody().string();
                }
                
                // Return empty lyrics with error
                return LyricsResponse.builder()
                        .trackName(trackName)
                        .artistName(artistName)
                        .lyrics("Lyrics not found")
                        .error("Failed to fetch lyrics: " + response.code() + " - " + errorMessage)
                        .build();
            }

        } catch (IOException e) {
            log.error("Error fetching lyrics", e);
            return LyricsResponse.builder()
                    .trackName(trackName)
                    .artistName(artistName)
                    .lyrics("Lyrics not found")
                    .error("Error fetching lyrics: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Cleans and normalizes text to improve lyrics search results
     * - Removes special characters and accents
     * - Removes text in parentheses and brackets (usually irrelevant for lyrics)
     * - Removes common terms like "feat.", "ft.", "remix", etc.
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // Remove text in parentheses and brackets
        text = text.replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "");
        
        // Remove common terms
        text = text.replaceAll("(?i)feat\\.|ft\\.|remix|remaster|version|edit|official|video|lyric|audio", "");
        
        // Normalize accents and diacritics
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        text = pattern.matcher(text).replaceAll("");
        
        // Remove special characters and trim
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        
        return text;
    }
} 