package com.example.soundCloud_BE.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MusixmatchApi {
    @GET("matcher.lyrics.get")
    Call<ResponseBody> getLyrics(
        @Query("q_track") String trackName,
        @Query("q_artist") String artistName,
        @Query("apikey") String apiKey
    );
} 