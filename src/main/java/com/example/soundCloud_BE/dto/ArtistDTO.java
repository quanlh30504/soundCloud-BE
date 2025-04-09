package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtistDTO {
    private String id;
    private String name;
    private List<ImageDTO> images;
    private List<String> genres;
    private Integer followers;
    private Integer popularity;
    private String spotifyUri;
    private String spotifyUrl;
    private List<AlbumDTO> albums;

    public static ArtistDTO fromArtist(Artist artist) {
        return ArtistDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .images(Arrays.stream(artist.getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .genres(Arrays.asList(artist.getGenres()))
                .followers(artist.getFollowers().getTotal())
                .popularity(artist.getPopularity())
                .spotifyUri(artist.getUri())
                .spotifyUrl(artist.getExternalUrls().get("spotify"))
                .build();
    }

    public static ArtistDTO fromArtistSimplified(ArtistSimplified artist) {
        return ArtistDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .spotifyUri(artist.getUri())
                .spotifyUrl(artist.getExternalUrls().get("spotify"))
                .build();
    }
} 