package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.AlbumDTO;
import com.example.soundCloud_BE.dto.ArtistDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.dto.DownloadResult;
import com.example.soundCloud_BE.dto.LyricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public Page<PlaylistDTO> searchPlaylists(String query, Pageable pageable) {
        try {
            authenticate();
            SearchPlaylistsRequest searchRequest = spotifyApi.searchPlaylists(query)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();

            Paging<PlaylistSimplified> searchResult = searchRequest.execute();
            log.info("Search query: {}", query);
            log.info("Total results found: {}", searchResult.getTotal());

            if (searchResult == null || searchResult.getItems() == null) {
                log.warn("No playlists found for query: {}", query);
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            List<PlaylistDTO> playlists = Arrays.stream(searchResult.getItems())
                    .filter(playlist -> playlist != null)
                    .map(playlist -> {
                        log.debug("Processing playlist: {}", playlist.getName());
                        return PlaylistDTO.fromPlaylistSimplified(playlist);
                    })
                    .collect(Collectors.toList());

            return new PageImpl<>(playlists, pageable, searchResult.getTotal());
        } catch (Exception e) {
            log.error("Error searching playlists: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search playlists: " + e.getMessage());
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

    public AlbumDTO getAlbum(String albumId, Pageable pageable) {
        authenticate();
        try {
            // Get album details
            GetAlbumRequest getAlbumRequest = spotifyApi.getAlbum(albumId).build();
            Album album = getAlbumRequest.execute();

            if (album == null) {
                return null;
            }

            // Get album tracks with pagination
            GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(albumId)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build();
            
            Paging<TrackSimplified> tracksPaging = getAlbumsTracksRequest.execute();
            List<TrackDTO> tracks = Arrays.stream(tracksPaging.getItems())
                    .map(track -> {
                        try {
                            // Get full track details
                            Track fullTrack = spotifyApi.getTrack(track.getId()).build().execute();
                            return TrackDTO.fromTrack(fullTrack);
                        } catch (Exception e) {
                            log.error("Error getting track details: {}", track.getId(), e);
                            return null;
                        }
                    })
                    .filter(track -> track != null)
                    .collect(Collectors.toList());

            // Create AlbumDTO with tracks
            AlbumDTO albumDTO = AlbumDTO.fromAlbum(album);
            albumDTO.setTracks(tracks);
            albumDTO.setTotalTracks(album.getTracks().getTotal());
            
            return albumDTO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get album: " + e.getMessage());
        }
    }

    public ArtistDTO getArtist(String artistId) {
        authenticate();
        try {
            // Get artist details
            GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
            Artist artist = getArtistRequest.execute();

            if (artist == null) {
                return null;
            }

            // Get artist's albums
            GetArtistsAlbumsRequest getArtistsAlbumsRequest = spotifyApi.getArtistsAlbums(artistId)
                    .limit(50)
                    .build();
            
            Paging<AlbumSimplified> albumsPaging = getArtistsAlbumsRequest.execute();
            List<AlbumDTO> albums = Arrays.stream(albumsPaging.getItems())
                    .map(AlbumDTO::fromAlbumSimplified)
                    .collect(Collectors.toList());

            // Create ArtistDTO with albums
            ArtistDTO artistDTO = ArtistDTO.fromArtist(artist);
            artistDTO.setAlbums(albums);
            
            return artistDTO;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get artist: " + e.getMessage());
        }
    }

    public PlaylistDTO getPlaylist(String playlistId, Pageable pageable) {
        authenticate();
        try {
            // Get playlist details
            Playlist playlist = spotifyApi.getPlaylist(playlistId).build().execute();

            if (playlist == null) {
                return null;
            }

            // Get playlist tracks with pagination
            Paging<PlaylistTrack> tracksPaging = spotifyApi.getPlaylistsItems(playlistId)
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .build()
                    .execute();

            List<TrackDTO> tracks = Arrays.stream(tracksPaging.getItems())
                    .map(playlistTrack -> {
                        try {
                            if (playlistTrack.getTrack() instanceof Track) {
                                Track track = (Track) playlistTrack.getTrack();
                                return TrackDTO.fromTrack(track);
                            }
                            return null;
                        } catch (Exception e) {
                            log.error("Error processing track in playlist: {}", e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(track -> track != null)
                    .collect(Collectors.toList());

            // Create PlaylistDTO with tracks
            PlaylistDTO playlistDTO = PlaylistDTO.fromPlaylist(playlist);
            playlistDTO.setTracks(tracks);
            playlistDTO.setTotalTracks(playlist.getTracks().getTotal());
            
            return playlistDTO;
        } catch (Exception e) {
            log.error("Failed to get playlist: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get playlist: " + e.getMessage());
        }
    }

} 