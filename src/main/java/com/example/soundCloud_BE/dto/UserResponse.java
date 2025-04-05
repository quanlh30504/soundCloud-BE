package com.example.soundCloud_BE.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
