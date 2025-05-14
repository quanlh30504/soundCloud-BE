package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.dto.DownloadResult;
import com.example.soundCloud_BE.dto.LyricsResponse;
import com.example.soundCloud_BE.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final SpotifyService spotifyService;

    @GetMapping("/lyricsOvh/{spotifyId}")
    public ResponseEntity<LyricsResponse> getTrackLyrics(@PathVariable String spotifyId) {
        LyricsResponse lyrics = spotifyService.getTrackLyricsOvh(spotifyId);
        return ResponseEntity.ok(lyrics);
    }

    @GetMapping("/lyricsZingMp3/{spotifyId}")
    public ResponseEntity<List<Map<String,String>>> getTrackLyricsZingMp3(@PathVariable String spotifyId) {
        return ResponseEntity.ok(spotifyService.getTrackLyricsZingMp3(spotifyId));
    }
    
    @PostMapping("/download/{spotifyId}")
    public CompletableFuture<ResponseEntity<DownloadResult>> downloadTrack(@PathVariable String spotifyId) {
        return spotifyService.downloadTrackAudio(spotifyId)
                .thenApply(result -> {
                    if (result.getSuccess()) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                });
    }
    
    
} 