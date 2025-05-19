package com.example.soundCloud_BE.dto;

import com.example.soundCloud_BE.model.Playlists;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.Arrays;
import java.util.Collections;
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
    private String description;
    private String ownerName;
    private String ownerId;
    private List<ImageDTO> images;
    private List<TrackDTO> tracks;
    private int totalTracks;
    private String spotifyUri;
    private String spotifyUrl;
    private Boolean isPublic;
    private Boolean isExternalPlaylist;
    private String externalPlaylistId;
    private Boolean collaborative;
    private String thumbnail;

    public static PlaylistDTO fromPlaylist(Playlist playlist) {
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .images(playlist.getImages() != null && playlist.getImages().length > 0 ?
                        Arrays.stream(playlist.getImages())
                                .map(image -> new ImageDTO(image.getHeight(), image.getWidth(), image.getUrl()))
                                .collect(Collectors.toList()) : Collections.emptyList())
                .ownerName(playlist.getOwner().getDisplayName())
                .totalTracks(playlist.getTracks().getTotal())
                .spotifyUri(playlist.getUri())
                .spotifyUrl(playlist.getExternalUrls().get("spotify"))
                .collaborative(playlist.getIsCollaborative())
                .build();
    }

    public static PlaylistDTO fromPlaylistSimplified(PlaylistSimplified playlist) {
        return PlaylistDTO.builder()
                .id(playlist.getId())
                .name(playlist.getName())
//                .description(playlist.getDescription())
                .images(playlist.getImages() != null && playlist.getImages().length > 0 ?
                        Arrays.stream(playlist.getImages())
                                .map(image -> new ImageDTO(image.getHeight(), image.getWidth(), image.getUrl()))
                                .collect(Collectors.toList()) : Collections.emptyList())
                .ownerName(playlist.getOwner().getDisplayName())
                .totalTracks(playlist.getTracks().getTotal())
                .spotifyUri(playlist.getUri())
                .spotifyUrl(playlist.getExternalUrls().get("spotify"))
                .collaborative(playlist.getIsCollaborative())
                .build();
    }
    public static PlaylistDTO fromEntity(Playlists playlists) {
        return PlaylistDTO.builder()
                .id(playlists.getId().toString()) // Map ID Spotify của Playlist
                .name(playlists.getName()) // Lấy tên của Playlist
                .description(playlists.getDescription()) // Mô tả Playlist
                .tracks(playlists.getTracks() != null && !playlists.getTracks().isEmpty()?
                        playlists.getTracks().stream()
                                .map(TrackDTO::fromEntity) // Chuyển đổi từng TrackEntity sang TrackDTO
                                .collect(Collectors.toList())
                        : Collections.emptyList()) // Danh sách bài hát rỗng nếu không có
                .totalTracks(playlists.getTracks() != null ? playlists.getTracks().size() : 0) // Tổng bài hát
                .isPublic(playlists.getIsPublic()) // Kiểm tra tính công khai
                .isExternalPlaylist(playlists.getIsExternalPlaylist()) // Kiểm tra Playlist bên ngoài
                .externalPlaylistId(playlists.getExternalPlaylistId()) // ID Playlist bên ngoài
                .thumbnail(playlists.getThumbnail()) // Ảnh đại diện Playlist
                .build();
    }
} 