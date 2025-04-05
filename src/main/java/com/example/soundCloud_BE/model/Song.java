package com.example.soundCloud_BE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "spotify_id", unique = true)
    private String spotifyId;

    @Column(name = "youtube_video_id", unique = true)
    private String youtubeVideoId;

    @Column(nullable = false)
    private String title;


    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    private Integer duration;
    private Date releaseDate;
    private String genre;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    private Integer fileSize;

    @Column(name = "download_status")
    private String downloadStatus = "PENDING";

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

