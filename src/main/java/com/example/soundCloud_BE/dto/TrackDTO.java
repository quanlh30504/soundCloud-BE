package com.example.soundCloud_BE.dto;

import com.example.soundCloud_BE.model.Tracks;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackDTO {
    private Integer id;
    private String spotifyId;
    private String name;
    private Integer durationMs;
    private String previewUrl;
    private List<String> artists;
    private List<String> artistIds;
    private String albumName;
    private String albumId;
    private List<ImageDTO> albumImages;
    private Integer popularity;
    private String releaseDate;
    private Boolean explicit;
    private String spotifyUri;
    private String spotifyUrl;
    private String filePath;
    private String downloadStatus;
    private String streamUrl;

    public static TrackDTO fromTrack(Track track) {
        return TrackDTO.builder()
                .spotifyId(track.getId())
                .name(track.getName())
                .durationMs(track.getDurationMs())
                .previewUrl(track.getPreviewUrl())
                .artists(Arrays.stream(track.getArtists())
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList()))
                .artistIds(Arrays.stream(track.getArtists())
                        .map(artist -> artist.getId())
                        .collect(Collectors.toList()))
                .albumName(track.getAlbum().getName())
                .albumId(track.getAlbum().getId())
                .albumImages(Arrays.stream(track.getAlbum().getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .popularity(track.getPopularity())
                .releaseDate(track.getAlbum().getReleaseDate())
                .explicit(track.getIsExplicit())
                .spotifyUri(track.getUri())
                .spotifyUrl(track.getExternalUrls().get("spotify"))
                .build();
    }

    public static TrackDTO fromEntity(Tracks track) {
        return TrackDTO.builder()
                .id(track.getId())
                .spotifyId(track.getSpotifyId()) // Maps 'spotifyId' from Tracks to 'id'
                .name(track.getTitle()) // Maps 'title' from Tracks to 'name'
                .albumImages(track.getCoverUrl() != null
                        ? List.of(ImageDTO.builder().url(track.getCoverUrl()).build())
                        : new ArrayList<>()) // Handles 'coverUrl' and maps it to 'albumImages' (adds as an ImageDTO)
                .artists(track.getArtists() != null && !track.getArtists().isEmpty()
                        ? Arrays.stream(track.getArtists().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList())
                        : new ArrayList<>()) // Splits and trims the artist string to map to the 'artists' field
                .filePath(track.getFilePath()) // Maps 'filePath'
                .downloadStatus(track.getDownloadStatus()) // Maps 'downloadStatus'
                .streamUrl(track.getStreamUrl()) // Maps 'streamUrl'
                .build();
    }
    
} 