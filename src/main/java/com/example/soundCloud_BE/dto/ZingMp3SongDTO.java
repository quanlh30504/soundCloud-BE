package com.example.soundCloud_BE.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ZingMp3SongDTO {
    private String id;
    private String title;
    private List<ZingArtistDTO> artists;
    private Integer duration;
    private String thumbnail;
    private List<String> streamUrls;
    private String lyrics;
    private ZingAlbumDTO album;
    private List<String> genres;
}

@Data
@Builder
class ZingArtistDTO {
    private String id;
    private String name;
    private String thumbnail;
}

@Data
@Builder
class ZingAlbumDTO {
    private String id;
    private String title;
    private String thumbnail;
    private List<ZingArtistDTO> artists;
}
