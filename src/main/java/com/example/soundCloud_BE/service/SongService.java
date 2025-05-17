package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.DownloadResult;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.repository.TrackRepository;
import com.example.soundCloud_BE.zingMp3.ZingMp3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongService {
    private final SpotifyService spotifyService;
    private final TrackRepository trackRepository;
    private final ZingMp3Service zingMp3Service;


    @Transactional
    public TrackDTO getSongInfo(String spotifyId) {
        try {
            // 1. Get track info from Spotify API
            TrackDTO track = spotifyService.getTrack(spotifyId);
            if (track == null) {
                log.error("Failed to get track info from Spotify for spotifyId: {}", spotifyId);
                throw new RuntimeException("Failed to get track info from Spotify");
            }

            // 2. Check if track exists in database
            Optional<Tracks> existingSong = trackRepository.findBySpotifyId(spotifyId);

            if (existingSong.isPresent()) {
                // 3a. If track exists, update filePath in TrackDTO
                Tracks tracks = existingSong.get();
                if (tracks.getFilePath() != null) {
                    track.setFilePath(tracks.getFilePath());
                    track.setDownloadStatus(tracks.getDownloadStatus());
                }

                if (tracks.getStreamUrl128() != null && !tracks.getStreamUrl128().isEmpty()) {
                    track.setStreamUrl(tracks.getStreamUrl128());
                } else {
                    final String zingId = spotifyService.convertSpotifyIdToZingId(spotifyId);
                    if (zingId != null) {
                        String streamUrl = zingMp3Service.getStreamingUrl(zingId);
                        if (streamUrl != null) {
                            track.setStreamUrl(streamUrl);
                        } else {
                            log.error("Failed to get streaming URL for Zing ID: {}", zingId);
                        }
                    } else {
                        log.error("Failed to convert Spotify ID to Zing ID for spotifyId: {}", spotifyId);
                    }

                }

                return track;
            } else {
                // 3b. If track doesn't exist, create new song record
                Tracks newTracks = Tracks.builder()
                        .title(track.getName())
                        .spotifyId(spotifyId)
                        .artists(track.getArtists().stream()
                                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b))
                        .coverUrl(track.getAlbumImages().isEmpty() ? null : track.getAlbumImages().get(0).getUrl())
                        .downloadStatus("pending")
                        .build();

                // 4. Save to database first to get the ID
//                newTracks = trackRepository.save(newTracks);
//                final Integer songId = newTracks.getId();
//
//                // 5. Start async download process
//                CompletableFuture.runAsync(() -> {
//                    try {
//                        downloadAndUpdateSong(songId, spotifyId);
//                    } catch (Exception e) {
//                        log.error("Unexpected error in download process for track: {}", spotifyId, e);
//                    }
//                });
//
                // Set initial status
                track.setDownloadStatus("pending");

                final String zingId = spotifyService.convertSpotifyIdToZingId(spotifyId);
                if (zingId != null) {
                    String streamUrl = zingMp3Service.getStreamingUrl(zingId);
                    if (streamUrl != null) {
                        track.setStreamUrl(streamUrl);
                        newTracks.setStreamUrl128(streamUrl);
                    } else {
                        log.error("Failed to get streaming URL for Zing ID: {}", zingId);
                    }
                } else {
                    log.error("Failed to convert Spotify ID to Zing ID for spotifyId: {}", spotifyId);
                }

                trackRepository.save(newTracks);
                return track;
            }
        } catch (Exception e) {
            log.error("Error in getSongInfo for spotifyId: {}", spotifyId, e);
            throw new RuntimeException("Failed to get song info: " + e.getMessage());
        }
    }

//    @Transactional
//    protected void downloadAndUpdateSong(Integer songId, String spotifyId) {
//        int maxRetries = 3;
//        int currentRetry = 0;
//        long delay = 1000; // 1 second
//
//        while (currentRetry < maxRetries) {
//            try {
//                // Get fresh entity in new transaction
//                Tracks tracks = trackRepository.findById(songId)
//                    .orElseThrow(() -> new RuntimeException("Song not found: " + songId));
//
//                // Download track audio
//                CompletableFuture<DownloadResult> downloadFuture = spotifyService.downloadTrackAudio(spotifyId);
//
//                try {
//                    // Wait for download to complete with timeout
//                    DownloadResult result = downloadFuture.get(5, TimeUnit.MINUTES);
//
//                    if (result != null && result.getFilePath() != null && !result.getFilePath().isEmpty()) {
//                        String filePath = result.getFilePath();
//                        log.info("Download completed. File path: {}", filePath);
//
//                        tracks.setFilePath(filePath);
//                        tracks.setDownloadStatus("completed");
//                        trackRepository.save(tracks);
//                        log.info("Successfully downloaded and saved track: {}", spotifyId);
//                        return;
//                    } else {
//                        tracks.setDownloadStatus("failed");
//                        trackRepository.save(tracks);
//                        log.error("Failed to download track: {}", spotifyId);
//                        throw new RuntimeException("Download failed for track: " + spotifyId);
//                    }
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    log.error("Error waiting for download to complete: {}", spotifyId, e);
//                    throw new RuntimeException("Download timeout or interrupted: " + e.getMessage());
//                }
//            } catch (Exception e) {
//                currentRetry++;
//                if (currentRetry == maxRetries) {
//                    log.error("Failed to download track after {} retries: {}", maxRetries, spotifyId, e);
//                    throw e;
//                }
//                try {
//                    Thread.sleep(delay * currentRetry);
//                } catch (InterruptedException ie) {
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException("Download interrupted", ie);
//                }
//            }
//        }
//    }
}
