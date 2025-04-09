package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.dto.DownloadResult;
import com.example.soundCloud_BE.dto.LyricsResponse;
import com.example.soundCloud_BE.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final SpotifyService spotifyService;

    @GetMapping("/lyrics/{trackId}")
    public ResponseEntity<LyricsResponse> getTrackLyrics(@PathVariable String trackId) {
        LyricsResponse lyrics = spotifyService.getTrackLyrics(trackId);
        return ResponseEntity.ok(lyrics);
    }
    
    @PostMapping("/download/{trackId}")
    public CompletableFuture<ResponseEntity<DownloadResult>> downloadTrack(@PathVariable String trackId) {
        return spotifyService.downloadTrackAudio(trackId)
                .thenApply(result -> {
                    if (result.getSuccess()) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                });
    }
} 