package com.example.soundCloud_BE.config;

import com.example.soundCloud_BE.api.MusixmatchApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class MusixmatchConfig {

    @Value("${musixmatch.api.baseUrl:https://api.musixmatch.com/ws/1.1/}")
    private String baseUrl;

    @Value("${musixmatch.api.key:your_api_key}")
    private String apiKey;

    @Bean
    public MusixmatchApi musixmatchApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(MusixmatchApi.class);
    }

    @Bean
    public String musixmatchApiKey() {
        return apiKey;
    }
} 