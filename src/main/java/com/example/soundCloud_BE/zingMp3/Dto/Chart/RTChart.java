package com.example.soundCloud_BE.zingMp3.Dto.Chart;

import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class RTChart {
    private List<SongData> promotes;
    private List<SongData> items;
    private Chart chart;


}
