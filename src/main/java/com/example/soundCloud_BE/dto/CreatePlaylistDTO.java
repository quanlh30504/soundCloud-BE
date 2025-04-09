package com.example.soundCloud_BE.dto;

import lombok.Data;

@Data
public class CreatePlaylistDTO {
    private String name;
    private String description;
    private boolean isPublic = false;
} 