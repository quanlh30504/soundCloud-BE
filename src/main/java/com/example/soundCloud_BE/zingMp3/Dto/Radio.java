package com.example.soundCloud_BE.zingMp3.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Radio {
    private String encodeId;
    private String title;
    private String thumbnail;
    private boolean isoffical;
    private String link;
    private boolean isIndie;
    private String releaseDate;
    private String sortDescription;
    private long releasedAt;
    private List<String> genreIds;
    @JsonProperty("PR")
    private boolean pr;
    private List<Artist> artists;
    private String artistsNames;
    private int playItemMode;
    private int subType;
    private long uid;
    private String thumbnailM;
    private boolean isShuffle;
    private boolean isPrivate;
    private String userName;
    private boolean isAlbum;
    private String textType;
    private boolean isSingle;
}