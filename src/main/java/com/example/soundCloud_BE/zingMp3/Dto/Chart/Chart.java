package com.example.soundCloud_BE.zingMp3.Dto.Chart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@NoArgsConstructor
public class Chart {
    private double minScore;
    private double maxScore;
    private long totalScore;
    private Map<String, List<ChartItemInfo>> items;
}
