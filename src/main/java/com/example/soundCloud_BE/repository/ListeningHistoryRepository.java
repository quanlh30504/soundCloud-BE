package com.example.soundCloud_BE.repository;

import com.example.soundCloud_BE.model.ListeningHistory;
import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Integer> {
    Page<ListeningHistory> findByUserOrderByListenedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT lh FROM ListeningHistory lh WHERE lh.user = ?1 AND lh.listenedAt >= ?2 ORDER BY lh.listenedAt DESC")
    Page<ListeningHistory> findRecentHistory(User user, LocalDateTime since, Pageable pageable);
    
    @Query("SELECT lh FROM ListeningHistory lh WHERE lh.user = ?1 ORDER BY lh.playCount DESC")
    Page<ListeningHistory> findMostPlayedTracks(User user, Pageable pageable);
    
    void deleteByUserAndTrack(User user, Tracks track);

    Optional<ListeningHistory> findByUserAndTrack(User user, Tracks track);
} 