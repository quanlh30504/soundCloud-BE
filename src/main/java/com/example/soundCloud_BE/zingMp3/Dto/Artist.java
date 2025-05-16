package com.example.soundCloud_BE.zingMp3.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Artist {
    private String id;
    private String name;
    private String alias;
    private String thumbnail;
    private String thumbnailM;
    private String biography;
    private String sortBiography;
    private String national;
    private String birthday;
    private String realname;
    private String totalFollow;
}