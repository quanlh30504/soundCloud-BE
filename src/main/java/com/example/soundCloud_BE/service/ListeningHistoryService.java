package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.ListeningHistoryDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.model.ListeningHistory;
import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.model.User;
import com.example.soundCloud_BE.repository.ListeningHistoryRepository;
import com.example.soundCloud_BE.repository.TrackRepository;
import com.example.soundCloud_BE.repository.UserRepository;
import com.google.api.gax.rpc.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListeningHistoryService {
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final UserRepository userRepository;
    private final TrackRepository tracksRepository;

    @Transactional
    public ListeningHistoryDTO addToHistory(String firebaseUid, String spotifyId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Tracks track = tracksRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        ListeningHistory history = listeningHistoryRepository.findByUserAndTrack(user, track)
                .orElse(new ListeningHistory());

        if (history.getId() == null) {
            history.setUser(user);
            history.setTrack(track);
            history.setPlayCount(1);
        } else {
            history.setPlayCount(history.getPlayCount() + 1);
        }
        
        history.setListenedAt(LocalDateTime.now());
        history = listeningHistoryRepository.save(history);
        return ListeningHistoryDTO.fromEntity(history);
    }

    public Page<ListeningHistoryDTO> getUserHistory(String firebaseUid, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        return listeningHistoryRepository.findByUserOrderByListenedAtDesc(user, pageable)
                .map(ListeningHistoryDTO::fromEntity);
    }

    public Page<ListeningHistoryDTO> getRecentHistory(String firebaseUid, LocalDateTime since, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        return listeningHistoryRepository.findRecentHistory(user, since, pageable)
                .map(ListeningHistoryDTO::fromEntity);
    }

    public Page<TrackDTO> getMostPlayedTracks(String firebaseUid, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        return listeningHistoryRepository.findMostPlayedTracks(user, pageable)
                .map(ListeningHistoryDTO::fromEntity)
                .map(ListeningHistoryDTO::getTrack);
    }

    @Transactional
    public void clearHistory(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        List<ListeningHistory> history = listeningHistoryRepository.findByUserOrderByListenedAtDesc(user, Pageable.unpaged())
                .getContent();
        
        listeningHistoryRepository.deleteAll(history);
    }

    @Transactional
    public void removeFromHistory(String firebaseUid, Integer trackId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Tracks track = tracksRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        
        listeningHistoryRepository.deleteByUserAndTrack(user, track);
    }
} 