package com.example.soundCloud_BE.zingMp3;


import com.example.soundCloud_BE.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/zingMp3")
public class ZingMp3Controller {
    public final ZingMp3Service zingMp3Service;
    public final SpotifyService spotifyService;

    @GetMapping("/{spotifyId}")
    public ResponseEntity<?> getZingMp3Id(@PathVariable String spotifyId) {
        return ResponseEntity.ok(spotifyService.convertSpotifyIdToZingId(spotifyId));
    }

    @GetMapping("/streamUrl/{zingId}")
    public ResponseEntity<?> getStreamingUrl(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3Service.getStreamingUrl(zingId));
    }

    @GetMapping("/lyrics/{zingId}")
    public ResponseEntity<?> getLyrics(@PathVariable String zingId) {
        return ResponseEntity.ok(zingMp3Service.getLyrics(zingId));
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFromStreamUrl(@RequestParam String url)
    {
        return ResponseEntity.ok(zingMp3Service.downloadSong(url));
    }
}
