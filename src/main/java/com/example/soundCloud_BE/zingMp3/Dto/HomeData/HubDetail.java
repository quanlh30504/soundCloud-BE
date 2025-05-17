package com.example.soundCloud_BE.zingMp3.Dto.HomeData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class HubDetail <T> {
    private String encodeId;
    private String cover;
    private String thumbnail;
    private String thumbnailHasText;
    private String title;
    private String description;
    private List<T> sections;
}
