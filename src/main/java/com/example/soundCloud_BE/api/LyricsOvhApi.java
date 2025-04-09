package com.example.soundCloud_BE.api;

import com.example.soundCloud_BE.dto.LyricsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface LyricsOvhApi {
    @GET("v1/{artist}/{title}")
    Call<LyricsResponse> getLyrics(
        @Path("artist") String artist,
        @Path("title") String title
    );
} 