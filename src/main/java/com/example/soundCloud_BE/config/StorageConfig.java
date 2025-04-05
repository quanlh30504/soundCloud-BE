package com.example.soundCloud_BE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class StorageConfig {

    @Value("${app.music.storage.path:music-downloads}")
    private String musicStoragePath;

    @Bean
    public String musicStoragePath() {
        // Create directory if it doesn't exist
        File directory = new File(musicStoragePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return musicStoragePath;
    }
} 