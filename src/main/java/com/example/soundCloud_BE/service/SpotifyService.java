package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.AlbumDTO;
import com.example.soundCloud_BE.dto.ArtistDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.dto.DownloadResult;
import com.example.soundCloud_BE.dto.LyricsResponse;
import com.example.soundCloud_BE.dto.CategoryDTO;
import com.example.soundCloud_BE.zingMp3.ZingMp3Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.CountryCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyService {

    @Value("${zingmp3.server.url:http://localhost:8088}")
    private String zingMp3ServerUrl;

    private final ObjectMapper objectMapper;


    private final RestTemplate restTemplate;
    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private final LyricsService lyricsService;
    private final YouTubeDownloadService youTubeDownloadService;
    private final ZingMp3Service zingMp3Service;

    public void authenticate() {
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

    public TrackDTO getTrack(String spotifyId) {
        authenticate();
        try {
            Track track = spotifyApi.getTrack(spotifyId).build().execute();
            return TrackDTO.fromTrack(track);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error getting track", e);
        }
    }
    
    public LyricsResponse getTrackLyricsOvh(String spotifyId) {
        TrackDTO track = getTrack(spotifyId);
        String trackName = track.getName();
        String artistName = track.getArtists().get(0); // Get first artist
        
        return lyricsService.getLyrics(trackName, artistName);
    }


    public List<Map<String, String>> getTrackLyricsZingMp3(String spotifyId) {

        String zingMp3Id = convertSpotifyIdToZingId(spotifyId);

        if (zingMp3Id == null) {
            log.error("Failed to convert Spotify ID to Zing ID for: {}", spotifyId);
            return Collections.emptyList();
        }

        return zingMp3Service.getLyrics(zingMp3Id);
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

    public List<CategoryDTO> getCategories() {
        authenticate();
        try {
            Paging<Category> categoryPaging = spotifyApi
                .getListOfCategories()
                .country(CountryCode.US)
                .limit(20)
                .build()
                .execute();

            return Arrays.stream(categoryPaging.getItems())
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Failed to fetch categories", e);
            throw new RuntimeException("Failed to fetch categories: " + e.getMessage());
        }
    }

    public List<PlaylistDTO> getPlaylistsByCategory(String categoryId) {
        authenticate();
        try {
            Paging<PlaylistSimplified> playlistPaging = spotifyApi
                .getCategorysPlaylists(categoryId)
                .country(CountryCode.US)
                .limit(20)
                .build()
                .execute();

            return Arrays.stream(playlistPaging.getItems())
                .map(PlaylistDTO::fromPlaylistSimplified)
                .collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Failed to fetch playlists for category: {}", categoryId, e);
            throw new RuntimeException("Failed to fetch playlists: " + e.getMessage());
        }
    }


    /**
     * Converts Spotify track ID to Zing MP3 song ID.
     *
     * @param spotifyId ID bài hát trên Spotify
     * @return Zing MP3 ID hoặc null nếu không tìm thấy
     */
    @Cacheable(value = "zingMp3IdMapping", key = "#spotifyId")
    public String convertSpotifyIdToZingId(String spotifyId) {
        try {
            // Get track info from Spotify
            var spotifyTrack = getTrack(spotifyId);
            if (spotifyTrack == null || spotifyTrack.getArtists() == null || spotifyTrack.getArtists().isEmpty()) {
                log.warn("Spotify track not found or has no artists for ID: {}", spotifyId);
                return null;
            }

            var artistsName = spotifyTrack.getArtists().stream()
                    .collect(Collectors.joining(", "));

            // Create search query
            String searchQuery = normalizeString(spotifyTrack.getName() + " " + artistsName) ;
            log.debug("Search query for Spotify ID {}: {}", spotifyId, searchQuery);

            // Build URL
            String searchUrl = UriComponentsBuilder
                    .fromHttpUrl(zingMp3ServerUrl + "/api/search")
                    .queryParam("keyword", URLEncoder.encode(searchQuery, StandardCharsets.UTF_8))
                    .build()
                    .toUriString();

            // Send request
            log.info("Sending search request to: {}", searchUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);

            // Check HTTP status
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Search request failed with status: {}", response.getStatusCode());
                throw new ZingMp3Service.ZingMp3Exception("Search request failed with status: " + response.getStatusCode());
            }

            // Parse response
            String responseBody = response.getBody();
            log.debug("Response body: {}", responseBody);
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Check for API error
            if (jsonNode.has("err") && jsonNode.get("err").asInt() != 0) {
                log.error("Zing MP3 API error: {}", jsonNode.get("msg").asText());
                throw new ZingMp3Service.ZingMp3Exception("Zing MP3 API error: " + jsonNode.get("msg").asText());
            }

            // Extract data
            JsonNode data = jsonNode.has("data") ? jsonNode.get("data") : null;
            if (data == null || !data.has("songs") || !data.get("songs").isArray() || data.get("songs").isEmpty()) {
                log.info("No matching songs found for query: {}", searchQuery);
                return null;
            }

            // Return the ID of the first matching song
            String zingId = data.get("songs").get(0).get("encodeId").asText("");
            log.info("Converted Spotify ID {} to Zing MP3 ID: {}", spotifyId, zingId);
            return zingId;

        } catch (ZingMp3Service.ZingMp3Exception e) {
            log.error("Error converting Spotify ID {}: {}", spotifyId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error converting Spotify ID {}: {}", spotifyId, e.getMessage(), e);
            throw new ZingMp3Service.ZingMp3Exception("Error converting Spotify ID to Zing MP3 ID: " + spotifyId, e);
        }
    }

    /**
     * Chuẩn hóa chuỗi: bỏ dấu, viết thường
     */
    private String normalizeString(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
    }

}
 