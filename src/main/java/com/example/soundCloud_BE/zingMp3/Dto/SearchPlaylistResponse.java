package com.example.soundCloud_BE.zingMp3.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class SearchPlaylistResponse {
    private List<Album> items;
}
