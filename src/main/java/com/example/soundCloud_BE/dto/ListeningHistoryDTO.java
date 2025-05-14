package com.example.soundCloud_BE.dto;

import com.example.soundCloud_BE.model.ListeningHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningHistoryDTO {
    private Integer id;
    private TrackDTO track;
    private LocalDateTime listenedAt;
    private Integer playCount;

    public static ListeningHistoryDTO fromEntity(ListeningHistory history) {
        return ListeningHistoryDTO.builder()
                .id(history.getId())
                .track(TrackDTO.fromEntity(history.getTrack()))
                .listenedAt(history.getListenedAt())
                .playCount(history.getPlayCount())
                .build();
    }
} 