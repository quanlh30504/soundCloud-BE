package com.example.soundCloud_BE.zingMp3;


import com.example.soundCloud_BE.service.SpotifyService;
import com.example.soundCloud_BE.zingMp3.Dto.SyncResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/zingMp3")
public class ZingMp3Controller {
    public final ZingMp3Service zingMp3Service;
    public final SpotifyService spotifyService;

    public final ZingMp3ApiService zingMp3ApiService;

    @GetMapping("/{spotifyId}")
    public ResponseEntity<?> getZingMp3Id(@PathVariable String spotifyId) {
        return ResponseEntity.ok(spotifyService.convertSpotifyIdToZingId(spotifyId));
    }

    @GetMapping("/streamUrl/{zingId}")
    public ResponseEntity<?> getStreamingUrl(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getStreamUrl(zingId));
    }

    @GetMapping("/songInfo/{zingId}")
    public ResponseEntity<?> getSongInfo(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getSongInfo(zingId));
    }

    @GetMapping("/lyrics/{zingId}")
    public ResponseEntity<?> getLyrics(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getLyrics(zingId));
    }

    @GetMapping("/search/multi")
    public ResponseEntity<?> searchMulti(@RequestParam(value = "query") String query) {
        return ResponseEntity.ok(zingMp3ApiService.searchMulti(query));
    }

    @GetMapping("/artist/info")
    public ResponseEntity<?> getArtistInfo(@RequestParam("alias") String alias) {
        return ResponseEntity.ok(zingMp3ApiService.getArtistInfo(alias));
    }

    @GetMapping("/artist/songs")
    public ResponseEntity<?> getSongsOfArtist(@RequestParam("artistId") String artistId,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "count", defaultValue = "15") int count
    ) {
        return ResponseEntity.ok(zingMp3ApiService.getListArtistSong(artistId, String.valueOf(page), String.valueOf(count)));
    }

    @GetMapping("/artist/playlists")
    public ResponseEntity<?> getPlaylistOfArtist(@RequestParam("artistId") String artistId,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "count", defaultValue = "15") int count
    ) {
        return ResponseEntity.ok(zingMp3ApiService.getListArtistPlaylist(artistId, String.valueOf(page), String.valueOf(count)));
    }

    @GetMapping("/playlist/info")
    public ResponseEntity<?> getPlaylistInfo(@RequestParam("id") String id) {
        return ResponseEntity.ok(zingMp3ApiService.getPlaylistInfo(id));
    }

    @GetMapping("/chart-home")
    public ResponseEntity<?> getChartHome() {
        return ResponseEntity.ok(zingMp3ApiService.getChartHome());
    }


    @PostMapping("/sync/{zingId}")
    public ResponseEntity<SyncResponse> syncSong(@PathVariable String zingId) {
        try {
            SyncResponse response = zingMp3ApiService.syncSongToDatabase(zingId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ bài hát với ID {}: {}", zingId, e.getMessage());
            return ResponseEntity.internalServerError().body(SyncResponse.failure("Internal server error"));
        }
    }



}
