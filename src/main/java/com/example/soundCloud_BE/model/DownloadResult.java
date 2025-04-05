package com.example.soundCloud_BE.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadResult {
    private String trackName;
    private String artistName;
    private String filePath;
    private Boolean success;
    private String errorMessage;
} 