package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.dto.CreatePlaylistDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.service.OwnPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/own-playlists")
@RequiredArgsConstructor
public class OwnPlaylistController {
    private final OwnPlaylistService ownPlaylistService;

    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @RequestHeader("X-Firebase-Uid") String firebaseUid,
            @RequestBody CreatePlaylistDTO request) {
        PlaylistDTO playlist = ownPlaylistService.createPlaylist(firebaseUid, request);
        return ResponseEntity.ok(playlist);
    }

    @PostMapping("/{playlistId}/tracks/{spotifyId}")
    public ResponseEntity<Void> addTrackToPlaylist(
            @PathVariable Integer playlistId,
            @PathVariable String spotifyId) {
        ownPlaylistService.addTrackToPlaylist(playlistId, spotifyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<Void> removeTrackFromPlaylist(
            @PathVariable Integer playlistId,
            @PathVariable Integer trackId) {
        ownPlaylistService.removeTrackFromPlaylist(playlistId, trackId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Integer playlistId,
            @RequestHeader("X-Firebase-Uid") String firebaseUid) {
        ownPlaylistService.deletePlaylist(playlistId, firebaseUid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PlaylistDTO>> getUserPlaylists(
            @RequestHeader("X-Firebase-Uid") String firebaseUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PlaylistDTO> playlists = ownPlaylistService.getUserPlaylists(firebaseUid, pageable);
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylist(@PathVariable Integer playlistId) {
        PlaylistDTO playlist = ownPlaylistService.getPlaylist(playlistId);
        return ResponseEntity.ok(playlist);
    }

    @GetMapping("/{playlistId}/tracks")
    public ResponseEntity<Page<TrackDTO>> getPlaylistTracks(
            @PathVariable Integer playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TrackDTO> tracks = ownPlaylistService.getPlaylistTracks(playlistId, pageable);
        return ResponseEntity.ok(tracks);
    }
} 