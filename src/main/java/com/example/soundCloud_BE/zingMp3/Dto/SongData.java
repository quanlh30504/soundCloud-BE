package com.example.soundCloud_BE.zingMp3.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class SongData {
    private String encodeId;
    private String title;
    private String alias;
//    private boolean isOffical;
//    private String username;
    private String artistsNames;
    private List<Artist> artists;
//    private boolean isWorldWide;
    private String thumbnailM;
    private String link;
    private String thumbnail;
    private int duration;
//    @JsonProperty("zingChoice")
//    private boolean zingChoice;
//    private boolean isPrivate;
//    private boolean preRelease;
//    private long releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate releaseDate;
    private List<String> genreIds;
    private String distributor;
//    private List<String> indicators;
//    private long radioId;
//    private boolean isIndie;
    private int streamingStatus;
//    private boolean allowAudioAds;
    private boolean hasLyric;
//    private long userid;
    private List<Genre> genres;
    private List<Composer> composers;
    private Album album;
//    private Radio radio;
//    @JsonProperty("isRBT")
//    private boolean isRBT;
//    private long like;
//    private long listen;
//    private boolean liked;
//    private int comment;
}