package com.example.soundCloud_BE.zingMp3.Dto.HomeData;

import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Top100 {
    private String sectionType;
    private String viewType;
    private String title;
    private List<SongData> items;
}
