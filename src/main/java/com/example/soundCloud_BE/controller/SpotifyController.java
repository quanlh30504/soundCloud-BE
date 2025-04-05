package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.dto.AlbumDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/search/tracks")
    public ResponseEntity<List<TrackDTO>> searchTracks(@RequestParam String query) {
        return ResponseEntity.ok(spotifyService.searchTracks(query));
    }

    @GetMapping("/search/albums")
    public ResponseEntity<List<AlbumDTO>> searchAlbums(@RequestParam String query) {
        return ResponseEntity.ok(spotifyService.searchAlbums(query));
    }

    @GetMapping("/search/playlists")
    public ResponseEntity<List<PlaylistDTO>> searchPlaylists(@RequestParam String query) {
        return ResponseEntity.ok(spotifyService.searchPlaylists(query));
    }

    @GetMapping("/tracks/{trackId}")
    public ResponseEntity<TrackDTO> getTrack(@PathVariable String trackId) {
        return ResponseEntity.ok(spotifyService.getTrack(trackId));
    }
} 