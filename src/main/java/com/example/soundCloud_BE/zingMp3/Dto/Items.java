package com.example.soundCloud_BE.zingMp3.Dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Items<T> {
    private String total;
    private List<T> items;
    private String page;
    private String count;
}
