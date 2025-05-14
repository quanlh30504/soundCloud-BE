package com.example.soundCloud_BE.zingMp3.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadRequest {
    String streamUrl;
}
