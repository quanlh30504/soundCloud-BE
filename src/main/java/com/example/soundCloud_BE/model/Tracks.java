package com.example.soundCloud_BE.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "tracks")
@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tracks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "spotify_id", unique = true)
    private String spotifyId;

    @Column(nullable = false)
    private String title;

    @Column(name = "artists")
    private String artists;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "download_status")
    private String downloadStatus = "pending";

    @Column(name = "stream_url_128", columnDefinition = "TEXT")
    private String streamUrl128;

    @Column(name = "stream_url_320", columnDefinition = "TEXT")
    private String streamUrl320;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

