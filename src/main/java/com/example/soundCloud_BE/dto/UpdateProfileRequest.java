package com.example.soundCloud_BE.dto;

import com.google.firebase.database.annotations.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {
    @NotNull
    private String displayName;

    private String avatarUrl; // Optional
}