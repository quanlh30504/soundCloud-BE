package com.example.soundCloud_BE.zingMp3.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class SearchMultiResponse {
    private SongData top;
    private List<SongData> songs;
    private List<Album> playlists;
    private List<Artist> artists;
}
