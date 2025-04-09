package com.example.soundCloud_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@Getter
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