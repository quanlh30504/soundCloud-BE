package com.example.soundCloud_BE.repository;

import com.example.soundCloud_BE.model.Playlists;
import com.example.soundCloud_BE.model.Tracks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Tracks, Integer> {
    Optional<Tracks> findById(Integer songId);
    Optional<Tracks> findByTitle(String title);
    Optional<Tracks> findBySpotifyId(String spotifyId);

}
