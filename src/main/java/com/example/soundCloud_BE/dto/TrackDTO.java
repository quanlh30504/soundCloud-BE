package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackDTO {
    private String id;
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

    public static TrackDTO fromTrack(Track track) {
        return TrackDTO.builder()
                .id(track.getId())
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
} 