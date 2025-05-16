package com.example.soundCloud_BE.zingMp3.Dto.Chart;

import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class WeekChartInfo {
    private String playlistId;
    private int chartId;
    private String cover;
    private String country;
    private String type;
    private String week;
    private String year;
    private String latestWeek;
    private String startDate;
    private String endDate;
    private List<SongData> items;

}
