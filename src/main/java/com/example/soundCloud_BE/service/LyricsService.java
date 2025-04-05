package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.api.MusixmatchApi;
import com.example.soundCloud_BE.model.LyricsResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LyricsService {

    private final MusixmatchApi musixmatchApi;
    private final String musixmatchApiKey;

    public LyricsResponse getLyrics(String trackName, String artistName) {
        try {
            Call<ResponseBody> call = musixmatchApi.getLyrics(trackName, artistName, musixmatchApiKey);
            Response<ResponseBody> response = call.execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Failed to fetch lyrics: " + response.message());
            }

            String jsonResponse = response.body().string();
            return parseLyricsResponse(jsonResponse, trackName, artistName);

        } catch (IOException e) {
            throw new RuntimeException("Error fetching lyrics", e);
        }
    }

    private LyricsResponse parseLyricsResponse(String jsonResponse, String trackName, String artistName) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject message = jsonObject.getAsJsonObject("message");
            JsonObject body = message.getAsJsonObject("body");
            
            if (body.size() == 0) {
                return LyricsResponse.builder()
                        .trackName(trackName)
                        .artistName(artistName)
                        .lyrics("Lyrics not found")
                        .build();
            }
            
            JsonObject lyrics = body.getAsJsonObject("lyrics");
            String lyricsBody = lyrics.get("lyrics_body").getAsString();
            String copyright = lyrics.get("lyrics_copyright").getAsString();
            
            return LyricsResponse.builder()
                    .trackName(trackName)
                    .artistName(artistName)
                    .lyrics(lyricsBody)
                    .copyright(copyright)
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Error parsing lyrics response", e);
        }
    }
} 