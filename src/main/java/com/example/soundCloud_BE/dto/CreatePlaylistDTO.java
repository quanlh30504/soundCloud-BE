package com.example.soundCloud_BE.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePlaylistDTO {
    private String name;
    private String description;
    private String thumbnail;
    private boolean isPublic = false;
    private boolean isExternalPlaylist = false;
    private String externalPlaylistId;
} 