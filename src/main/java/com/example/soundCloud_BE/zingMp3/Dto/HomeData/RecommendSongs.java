package com.example.soundCloud_BE.zingMp3.Dto.HomeData;

import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class RecommendSongs {
    private List<SongData> items;
}
