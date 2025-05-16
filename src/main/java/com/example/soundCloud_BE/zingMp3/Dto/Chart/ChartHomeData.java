package com.example.soundCloud_BE.zingMp3.Dto.Chart;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ChartHomeData {
    @JsonProperty("RTChart")
    private RTChart RTChart;
    private Map<String, WeekChartInfo> weekChart;
}