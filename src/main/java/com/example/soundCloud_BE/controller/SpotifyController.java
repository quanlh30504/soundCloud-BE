package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.dto.AlbumDTO;
import com.example.soundCloud_BE.dto.ArtistDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.service.SongService;
import com.example.soundCloud_BE.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SongService songService;

    @GetMapping("/search/tracks")
    public ResponseEntity<List<TrackDTO>> searchTracks(@RequestParam String query) {
        return ResponseEntity.ok(spotifyService.searchTracks(query));
    }

    @GetMapping("/search/albums")
    public ResponseEntity<List<AlbumDTO>> searchAlbums(@RequestParam String query) {
        return ResponseEntity.ok(spotifyService.searchAlbums(query));
    }

    @GetMapping("/search/playlists")
    public ResponseEntity<Page<PlaylistDTO>> searchPlaylists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? 
            Sort.by(Sort.Direction.DESC, "name") : 
            Sort.by(Sort.Direction.ASC, "name");
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PlaylistDTO> results = spotifyService.searchPlaylists(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tracks/{trackId}")
    public ResponseEntity<?> getTrackInfo(@PathVariable String trackId) {
        try {
            TrackDTO track = songService.getSongInfo(trackId);
            return ResponseEntity.ok(track);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/albums/{albumId}")
    public ResponseEntity<AlbumDTO> getAlbumInfo(
            @PathVariable String albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        AlbumDTO album = spotifyService.getAlbum(albumId, pageable);
        
        if (album == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(album);
    }

    @GetMapping("/artists/{artistId}")
    public ResponseEntity<?> getArtistInfo(@PathVariable String artistId) {
        try {
            ArtistDTO artist = spotifyService.getArtist(artistId);
            if (artist == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(artist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/playlists/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylistInfo(
            @PathVariable String playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PlaylistDTO playlist = spotifyService.getPlaylist(playlistId, pageable);
        
        if (playlist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(playlist);
    }
} 