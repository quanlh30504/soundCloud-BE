package com.example.soundCloud_BE.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lyrics")
@Data
public class Lyric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    private String language;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String source; // Spotify, Musixmatch...

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
