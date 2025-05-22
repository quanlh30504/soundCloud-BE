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
import com.example.soundCloud_BE.zingMp3.Dto.Album;
import com.example.soundCloud_BE.zingMp3.Dto.SongData;
import com.example.soundCloud_BE.zingMp3.ZingMp3ApiService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnPlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TrackRepository tracksRepository;
    private final UserRepository userRepository;

    private final ZingMp3ApiService zingMp3ApiService;

    @Transactional
    public PlaylistDTO createPlaylist(String firebaseUid, CreatePlaylistDTO request) {

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getName() == null || request.getName().isEmpty()) {
            throw new IllegalArgumentException("Playlist name cannot be empty");
        }

        if (request.getName().equals("SYS_LIKED_TRACKS")) {
            throw new IllegalStateException("You cannot create a playlist with this name");
        }

        if (request.isExternalPlaylist() ) {
            Optional<Playlists> existingPlaylist = playlistRepository.findByExternalPlaylistId(request.getExternalPlaylistId(), user.getId());
            if (existingPlaylist.isPresent()) {
                throw new IllegalStateException("Playlist with this external ID already exists");
            }
        }

        List<Playlists> existNamePlaylist = playlistRepository.findByUserIdAndNameContaining(user.getId(), request.getName());
        if (existNamePlaylist != null && !existNamePlaylist.isEmpty()) {
            throw new IllegalStateException("Playlist with this name already exists");
        }

        Playlists playlists = Playlists.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .isPublic(request.isPublic())
                .externalPlaylistId(request.getExternalPlaylistId())
                .isExternalPlaylist(request.isExternalPlaylist())
                .thumbnail(request.getThumbnail())
                .build();

        playlists = playlistRepository.save(playlists);
        return PlaylistDTO.builder()
                .id(playlists.getId().toString())
                .name(playlists.getName())
                .description(playlists.getDescription())
                .isPublic(playlists.getIsPublic())
                .isExternalPlaylist(playlists.getIsExternalPlaylist())
                .externalPlaylistId(playlists.getExternalPlaylistId())
                .thumbnail(playlists.getThumbnail())
                .build();
    }

    @Transactional
    public void addTrackToPlaylist(Integer playlistId, String spotifyId) {
        Playlists playlists = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        
        Tracks track = tracksRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        if (playlistRepository.existsTrackInPlaylistBySpotifyId(playlistId, spotifyId)) {
            log.info("Track " + spotifyId + " already exists in the playlist");
            return;
        }
        List<Tracks> tracksList = playlists.getTracks();
        if (tracksList == null) {
            tracksList = new ArrayList<>();
            tracksList.add(track);
        }else {
            tracksList.add(track);
        }
        playlists.setTracks(tracksList);
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

        if (playlists.getName().equals("SYS_LIKED_TRACKS")) {
            throw new IllegalStateException("You cannot delete the system playlist");
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


     // Create playlist from external playlist
    public PlaylistDTO createPlaylistFromExternal(String firebaseUid, String externalPlaylistId) {
//        PlaylistDTO playlistDTO = createPlaylist(firebaseUid, request);

        Album externalPlaylist = zingMp3ApiService.getPlaylistInfo(externalPlaylistId);
        if (externalPlaylist == null) {
            throw new IllegalArgumentException("External playlist not found");
        }

        CreatePlaylistDTO request = new CreatePlaylistDTO();
        request.setName(externalPlaylist.getTitle());
        request.setDescription(externalPlaylist.getSortDescription());
        request.setPublic(false);
        request.setExternalPlaylistId(externalPlaylistId);
        request.setExternalPlaylist(true);
        request.setThumbnail(externalPlaylist.getThumbnail());

        PlaylistDTO playlistDTO = createPlaylist(firebaseUid, request);

        List<SongData> songs = externalPlaylist.getSong().getItems();
        for (SongData song : songs) {
            zingMp3ApiService.syncSongToDatabaseWithBody(song.getEncodeId(), song);
            addTrackToPlaylist(Integer.valueOf(playlistDTO.getId()), song.getEncodeId());
        }
        playlistDTO.setTotalTracks(songs.size());
        return playlistDTO;

    }

    // Check exist external playlist
    public boolean checkExistExternalPlaylist(String firebaseUid, String externalPlaylistId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Optional<Playlists> playlists = playlistRepository.findByExternalPlaylistId(externalPlaylistId, user.getId());
        if (playlists.isPresent()) {
            return true;
        }
        return false;
    }


    public void createLikedTracks(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlists = new Playlists();
        playlists.setName("SYS_LIKED_TRACKS");
        playlists.setUser(user);
        playlists.setIsPublic(false);

        playlistRepository.save(playlists);
    }

    public Page<TrackDTO> getLikedTracks(String firebaseUid, Pageable pageable) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                .orElse(null);

        if (playlist == null) {
            createLikedTracks(firebaseUid);
            playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        } else if (!playlist.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new IllegalStateException("You don't have permission to access this playlist");
        }

        List<TrackDTO> tracks = playlist.getTracks().stream().map(TrackDTO::fromEntity).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), tracks.size());
        List<TrackDTO> tracksPage = tracks.subList(start, end);

        return new PageImpl<>(tracksPage, pageable, tracks.size());
    }

    @Transactional
    public void addTrackToLikedTracks(String firebaseUid, String spotifyId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                .orElse(null);

        if (playlist == null) {
            createLikedTracks(firebaseUid);
            playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                    .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        } else if (!playlist.getUser().getFirebaseUid().equals(firebaseUid)) {
            throw new IllegalStateException("You don't have permission to access this playlist");
        }

        Tracks track = tracksRepository.findBySpotifyId(spotifyId)
                .orElse(null);
        if (track == null) {
            zingMp3ApiService.syncSongToDatabase(spotifyId);
            track = tracksRepository.findBySpotifyId(spotifyId)
                    .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        }
        Tracks finalTrack = track;

        // check if the track is already in the playlist
        if (playlist.getTracks().stream().anyMatch(t -> t.getId().equals(finalTrack.getId()))) {
            throw new IllegalStateException("Track already exists in the playlist");
        } else {
            playlist.getTracks().add(track);
        }
        playlistRepository.save(playlist);
    }

    @Transactional
    public void removeTrackFromLikedTracks(String firebaseUid, String spotifyId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        playlist.getTracks().removeIf(track -> track.getSpotifyId().equals(spotifyId));
        playlistRepository.save(playlist);
    }

    public Boolean isTrackInLikedTracks(String firebaseUid, String spotifyId) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Playlists playlist = playlistRepository.findByNameAndUser("SYS_LIKED_TRACKS", user)
                .orElse(null);

        if (playlist == null) {
            return false;
        }
        return playlist.getTracks().stream().anyMatch(track -> track.getSpotifyId().equals(spotifyId));
    }
} 