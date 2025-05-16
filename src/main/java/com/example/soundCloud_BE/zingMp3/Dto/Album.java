package com.example.soundCloud_BE.zingMp3.Dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Album {
    private String encodeId;
    private String title;
    private String aliasTitle;

    private String thumbnail;
    private String thumbnailM;

    private String releaseDate;
    private String sortDescription;
    private long releasedAt;

    private List<String> genreIds;
    private List<Genre> genres;

    private List<Artist> artists;
    private String artistsNames;

    private String distributor;

    private Items<SongData> song;



}
