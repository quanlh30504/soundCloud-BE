package com.example.soundCloud_BE.zingMp3.Dto.Chart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ChartItemInfo {
    private long time;
    private String hour;
    private long counter;
}
