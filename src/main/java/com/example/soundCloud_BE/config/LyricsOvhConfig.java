package com.example.soundCloud_BE.config;

import com.example.soundCloud_BE.api.LyricsOvhApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class LyricsOvhConfig {

    @Value("${lyrics.ovh.baseUrl:https://api.lyrics.ovh/}")
    private String baseUrl;

    @Bean
    public LyricsOvhApi lyricsOvhApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(LyricsOvhApi.class);
    }
} 