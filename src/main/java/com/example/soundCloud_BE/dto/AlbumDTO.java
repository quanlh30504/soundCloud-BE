package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Image;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumDTO {
    private String id;
    private String name;
    private String albumType;
    private List<String> artists;
    private List<String> artistIds;
    private List<ImageDTO> images;
    private String releaseDate;
    private Integer totalTracks;
    private String spotifyUri;
    private String spotifyUrl;
    private List<TrackDTO> tracks;


    public static AlbumDTO fromAlbum(Album album) {
        return AlbumDTO.builder()
                .id(album.getId())
                .name(album.getName())
                .albumType(album.getAlbumType().getType())
                .artists(Arrays.stream(album.getArtists())
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList()))
                .artistIds(Arrays.stream(album.getArtists())
                        .map(artist -> artist.getId())
                        .collect(Collectors.toList()))
                .images(Arrays.stream(album.getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .releaseDate(album.getReleaseDate())
//                .totalTracks(album.getTracks().getTotal())
                .spotifyUri(album.getUri())
                .spotifyUrl(album.getExternalUrls().get("spotify"))
                .build();
    }

    public static AlbumDTO fromAlbumSimplified(AlbumSimplified album) {
        return AlbumDTO.builder()
                .id(album.getId())
                .name(album.getName())
                .albumType(album.getAlbumType().getType())
                .artists(Arrays.stream(album.getArtists())
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList()))
                .artistIds(Arrays.stream(album.getArtists())
                        .map(artist -> artist.getId())
                        .collect(Collectors.toList()))
                .images(Arrays.stream(album.getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .releaseDate(album.getReleaseDate())
                .spotifyUri(album.getUri())
                .spotifyUrl(album.getExternalUrls().get("spotify"))
                .build();
    }
} 