package com.example.soundCloud_BE.repository;

import com.example.soundCloud_BE.model.Playlists;
import com.example.soundCloud_BE.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlists, Integer> {
    Page<Playlists> findByUser(User user, Pageable pageable);
}
