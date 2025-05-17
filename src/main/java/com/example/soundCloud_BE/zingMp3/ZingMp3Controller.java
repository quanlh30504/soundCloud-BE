package com.example.soundCloud_BE.zingMp3;


import com.example.soundCloud_BE.dto.ListeningHistoryDTO;
import com.example.soundCloud_BE.service.ListeningHistoryService;
import com.example.soundCloud_BE.service.SpotifyService;
import com.example.soundCloud_BE.zingMp3.Dto.HomeData.HubDetail;
import com.example.soundCloud_BE.zingMp3.Dto.HomeData.Top100;
import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.example.soundCloud_BE.zingMp3.Dto.SyncResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/zingMp3")
public class ZingMp3Controller {
    public final ZingMp3ApiService zingMp3ApiService;
    public final ListeningHistoryService listeningHistoryService;


    @GetMapping("/song/streamUrl/{zingId}")
    public ResponseEntity<?> getStreamingUrl(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getStreamUrl(zingId));
    }

    @GetMapping("/song/info/{zingId}")
    public ResponseEntity<?> getSongInfo(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getSongInfo(zingId));
    }

    @GetMapping("/song/lyrics/{zingId}")
    public ResponseEntity<?> getLyrics(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3ApiService.getLyrics(zingId));
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


    @PostMapping("/history/{zingId}")
    public ResponseEntity<ListeningHistoryDTO> addToHistory(
            @PathVariable String zingId,
            @RequestHeader("X-Firebase-Uid") String firebaseUid) {
        log.info("Adding track {} to history for user {}", zingId, firebaseUid);
        ListeningHistoryDTO history = listeningHistoryService.addToHistory(firebaseUid, zingId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ListeningHistoryDTO>> getUserHistory(
            @RequestHeader("X-Firebase-Uid") String firebaseUid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ListeningHistoryDTO> history = listeningHistoryService.getUserHistory(firebaseUid, pageable);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(@RequestHeader("X-Firebase-Uid") String firebaseUid) {
        listeningHistoryService.clearHistory(firebaseUid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/history/tracks/{zingId}")
    public ResponseEntity<Void> removeFromHistory(
            @PathVariable String zingId,
            @RequestHeader("X-Firebase-Uid") String firebaseUid) {
        listeningHistoryService.removeFromHistory(firebaseUid, zingId);
        return ResponseEntity.ok().build();
    }


    // API home

    // Get top 100 songs
    @GetMapping("/home/top100")
    public ResponseEntity<List<Top100>> getTop100() {
        return ResponseEntity.ok(zingMp3ApiService.getTop100());
    }

    // Get top chill songs in hub detail
    @GetMapping("/home/hub-detail/chill")
    public ResponseEntity<HubDetail<Top100>> getHubDetailChill() {
        return ResponseEntity.ok(zingMp3ApiService.getHubDetail("IWZ9Z0CI"));
    }

    //Get recommend songs
    @GetMapping("/home/recommend")
    public ResponseEntity<List<SongData>> getRecommendSongs() {
        return ResponseEntity.ok(zingMp3ApiService.getRecommendSongs());
    }

    //Get new release song (type = song) or new release album (type = album)
    @GetMapping("/home/new-release")
    public ResponseEntity<?> getNewRelease(@RequestParam("type") String type) {
        return ResponseEntity.ok(zingMp3ApiService.getNewRelease(type));
    }

    //Get BXH nhạc mới (Top 100) song
    @GetMapping("/home/new-release/top100")
    public ResponseEntity<?> getNewReleaseTop100() {
        return ResponseEntity.ok(zingMp3ApiService.getNewReleaseChart());
    }

}
