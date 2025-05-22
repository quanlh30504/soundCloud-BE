package com.example.soundCloud_BE.repository;

import com.example.soundCloud_BE.model.Playlists;
import com.example.soundCloud_BE.model.Tracks;
import com.example.soundCloud_BE.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlists, Integer> {

    // Tìm playlist theo user với phân trang
    Page<Playlists> findByUser(User user, Pageable pageable);

    // Tìm playlist theo user id
    List<Playlists> findByUserId(Integer userId);

    // Tìm playlist công khai
    List<Playlists> findByIsPublicTrue();

    // Tìm playlist theo external playlist id và user id
    @Query("SELECT p FROM Playlists p WHERE p.externalPlaylistId = :externalPlaylistId AND p.user.id = :userId")
    Optional<Playlists> findByExternalPlaylistId(@Param("externalPlaylistId") String externalPlaylistId, @Param("userId") Integer userId);

    // Tìm playlist theo tên (có phân biệt hoa thường)
    List<Playlists> findByNameContaining(String name);

    // Tìm playlist theo user và tên playlist (có phân biệt hoa thường)
    List<Playlists> findByUserIdAndNameContaining(Integer userId, String name);

    // Tìm các playlist chứa bài hát cụ thể
    @Query("SELECT p FROM Playlists p JOIN p.tracks t WHERE t = :track")
    List<Playlists> findPlaylistsByTrack(@Param("track") Tracks track);

    // Tìm các playlist chứa bài hát theo tên bài hát (không phân biệt hoa thường)
    @Query("SELECT p FROM Playlists p JOIN p.tracks t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :songName, '%'))")
    List<Playlists> findPlaylistsBySongName(@Param("songName") String songName);

    // Kiểm tra xem playlist có tồn tại không
    boolean existsByIdAndUserId(Integer playlistId, Integer userId);

    // Tìm tất cả tracks trong một playlist cụ thể
    @Query("SELECT t FROM Playlists p JOIN p.tracks t WHERE p.id = :playlistId")
    List<Tracks> findTracksByPlaylistId(@Param("playlistId") Integer playlistId);

    // Kiểm tra xem track với spotifyId có tồn tại trong playlist cụ thể không
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Playlists p JOIN p.tracks t WHERE p.id = :playlistId AND t.spotifyId = :spotifyId")
    boolean existsTrackInPlaylistBySpotifyId(@Param("playlistId") Integer playlistId, @Param("spotifyId") String spotifyId);

    Playlists findByName(String name);
    Optional<Playlists> findByNameAndUser(String name, User user);
}
