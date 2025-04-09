package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LyricsResponse {
    private String lyrics;
    private String error;
    
    // Additional fields not in Lyrics.ovh API but useful for our application
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String trackName;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String artistName;
} 