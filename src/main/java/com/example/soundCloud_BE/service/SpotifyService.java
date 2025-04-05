package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.AlbumDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.model.DownloadResult;
import com.example.soundCloud_BE.model.LyricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private final LyricsService lyricsService;
    private final YouTubeDownloadService youTubeDownloadService;

    private void authenticate() {
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error authenticating with Spotify", e);
        }
    }

    public List<TrackDTO> searchTracks(String query) {
        authenticate();
        try {
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(query)
                    .limit(15)
                    .build();

            return Arrays.stream(searchTracksRequest.execute().getItems())
                    .map(TrackDTO::fromTrack)
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error searching tracks", e);
        }
    }

    public List<AlbumDTO> searchAlbums(String query) {
        authenticate();
        try {
            SearchAlbumsRequest searchAlbumsRequest = spotifyApi.searchAlbums(query)
                    .limit(15)
                    .build();

            return Arrays.stream(searchAlbumsRequest.execute().getItems())
                    .map(AlbumDTO::fromAlbumSimplified)
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error searching albums", e);
        }
    }

    public List<PlaylistDTO> searchPlaylists(String query) {
        authenticate();
        try {
            SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query)
                    .limit(15)
                    .build();

            return Arrays.stream(searchPlaylistsRequest.execute().getItems())
                    .map(PlaylistDTO::fromPlaylistSimplified)
                    .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error searching playlists", e);
        }
    }

    public TrackDTO getTrack(String trackId) {
        authenticate();
        try {
            Track track = spotifyApi.getTrack(trackId).build().execute();
            return TrackDTO.fromTrack(track);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error getting track", e);
        }
    }
    
    public LyricsResponse getTrackLyrics(String trackId) {
        TrackDTO track = getTrack(trackId);
        String trackName = track.getName();
        String artistName = track.getArtists().get(0); // Get first artist
        
        return lyricsService.getLyrics(trackName, artistName);
    }
    
    public CompletableFuture<DownloadResult> downloadTrackAudio(String trackId) {
        TrackDTO track = getTrack(trackId);
        String trackName = track.getName();
        String artistName = track.getArtists().get(0); // Get first artist
        
        return youTubeDownloadService.downloadSong(trackName, artistName);
    }
} 