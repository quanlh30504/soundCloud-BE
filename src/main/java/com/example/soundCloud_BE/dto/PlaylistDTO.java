package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaylistDTO {
    private String id;
    private String name;
//    private String description;
    private String ownerName;
    private String ownerId;
    private List<ImageDTO> images;
    private Integer totalTracks;
    private String spotifyUri;
    private String spotifyUrl;
    private Boolean isPublic;
    private Boolean collaborative;


    public static PlaylistDTO fromPlaylist(Playlist playlist) {
        User owner = playlist.getOwner();
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
//                .description(playlist.getDescription())
                .ownerName(owner != null ? owner.getDisplayName() : null)
                .ownerId(owner != null ? owner.getId() : null)
                .images(Arrays.stream(playlist.getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .totalTracks(playlist.getTracks() != null ? playlist.getTracks().getTotal() : 0)
                .spotifyUri(playlist.getUri())
                .spotifyUrl(playlist.getExternalUrls().get("spotify"))
                .isPublic(playlist.getIsPublicAccess())
                .collaborative(playlist.getIsCollaborative())
                .build();
    }

    public static PlaylistDTO fromPlaylistSimplified(PlaylistSimplified playlist) {
        User owner = playlist.getOwner();
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
//                .description(playlist.getDescription())
                .ownerName(owner != null ? owner.getDisplayName() : null)
                .ownerId(owner != null ? owner.getId() : null)
                .images(Arrays.stream(playlist.getImages())
                        .map(image -> ImageDTO.builder()
                                .height(image.getHeight())
                                .width(image.getWidth())
                                .url(image.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .totalTracks(playlist.getTracks() != null ? playlist.getTracks().getTotal() : 0)
                .spotifyUri(playlist.getUri())
                .spotifyUrl(playlist.getExternalUrls().get("spotify"))
                .isPublic(playlist.getIsPublicAccess())
                .collaborative(playlist.getIsCollaborative())
                .build();
    }
} 