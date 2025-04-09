package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.CreatePlaylistDTO;
import com.example.soundCloud_BE.dto.PlaylistDTO;
import com.example.soundCloud_BE.dto.TrackDTO;
import com.example.soundCloud_BE.model.Playlists;
import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.model.User;
import com.example.soundCloud_BE.repository.PlaylistRepository;
import com.example.soundCloud_BE.repository.TrackRepository;
import com.example.soundCloud_BE.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnPlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackRepository tracksRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlaylistDTO createPlaylist(String firebaseUid, CreatePlaylistDTO request) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlists = new Playlists();
        playlists.setName(request.getName());
        playlists.setDescription(request.getDescription());
        playlists.setUser(user);
        playlists.setIsPublic(request.isPublic());
        
        playlists = playlistRepository.save(playlists);
        return PlaylistDTO.builder()
                .id(playlists.getId().toString())
                .name(playlists.getName())
                .description(playlists.getDescription())
                .isPublic(playlists.getIsPublic())
                .build();
    }

    @Transactional
    public void addTrackToPlaylist(Integer playlistId, String spotifyId) {
        Playlists playlists = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        
        Tracks track = tracksRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        
        playlists.getTracks().add(track);
        playlistRepository.save(playlists);
    }

    @Transactional
    public void removeTrackFromPlaylist(Integer playlistId, Integer trackId) {
        Playlists playlists = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        
        playlists.getTracks().removeIf(track -> track.getId().equals(trackId));
        playlistRepository.save(playlists);
    }

    @Transactional
    public void deletePlaylist(Integer playlistId, String firebaseUid) {
        Playlists playlists = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        
        if (!playlists.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new IllegalStateException("You don't have permission to delete this playlist");
        }
        
        playlistRepository.delete(playlists);
    }

    public Page<PlaylistDTO> getUserPlaylists(String firebaseUid, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Page<Playlists> playlists = playlistRepository.findByUser(user, pageable);
        return playlists.map(PlaylistDTO::fromEntity);
    }

    public Page<TrackDTO> getPlaylistTracks(Integer playlistId, Pageable pageable) {
        Playlists playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        List<TrackDTO> tracks = playlist.getTracks().stream().map(TrackDTO::fromEntity).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), tracks.size());
        List<TrackDTO> tracksPage = tracks.subList(start, end);

        return new PageImpl<>(tracksPage, pageable, tracks.size());
    }

    public PlaylistDTO getPlaylist(Integer playlistId) {
        Playlists playlists = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        return PlaylistDTO.fromEntity(playlists);
    }
} 