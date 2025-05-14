package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.DownloadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeDownloadService {

    private final String musicStoragePath;
    private final String ytDlpExecutablePath;
    @Value("${ffmpeg.executable-path:}")
    private final String ffmpegExecutablePath;

    public CompletableFuture<DownloadResult> downloadSong(String trackName, String artistName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (ytDlpExecutablePath == null || ytDlpExecutablePath.isEmpty()) {
                    return DownloadResult.builder()
                            .trackName(trackName)
                            .artistName(artistName)
                            .success(false)
                            .errorMessage("yt-dlp path is not configured or could not be found/installed. Please install yt-dlp manually.")
                            .build();
                }
                
                // Create search query
                String searchQuery = trackName + " " + artistName + " audio";
                
                // Create a filename based on track and artist name
                String safeFileName = trackName.replaceAll("[^a-zA-Z0-9]", "_") + 
                        "_" + artistName.replaceAll("[^a-zA-Z0-9]", "_");
                
                // Create output directory if it doesn't exist
                File outputDir = new File(musicStoragePath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                
                // Build yt-dlp command
                List<String> command = new ArrayList<>();
                
                // Command with full path
                command.add(ytDlpExecutablePath);
                
                // Search YouTube and get the first result
                command.add("ytsearch1:" + searchQuery);
                
                // Extract audio only
                command.add("-x");
                
                // Convert to mp3
                command.add("--audio-format");
                command.add("mp3");
                
                // Set audio quality (0 is best, 9 is worst)
                command.add("--audio-quality");
                command.add("0");
                
                // Output template
                command.add("-o");
                command.add(musicStoragePath + File.separator + safeFileName + ".%(ext)s");
                
                // Don't overwrite existing files
                command.add("--no-overwrites");
                
                // Embed thumbnail in audio file
                command.add("--embed-thumbnail");
                
                // Embed metadata in file
                command.add("--embed-metadata");

                command.add("--ffmpeg-location");
                command.add(ffmpegExecutablePath);

                // Execute the process
                log.info("Executing command: {}", String.join(" ", command));
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                
                // Read output
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                        log.info("yt-dlp: {}", line);
                    }
                }
                
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("yt-dlp exited with code {}: {}", exitCode, output);
                    return DownloadResult.builder()
                            .trackName(trackName)
                            .artistName(artistName)
                            .success(false)
                            .errorMessage("Error downloading: process exited with code " + exitCode)
                            .build();
                }
                
                // Check if the file exists
                File downloadedFile = new File(musicStoragePath, safeFileName + ".mp3");
                if (!downloadedFile.exists()) {
                    return DownloadResult.builder()
                            .trackName(trackName)
                            .artistName(artistName)
                            .success(false)
                            .errorMessage("Download completed but file not found")
                            .build();
                }
                
                return DownloadResult.builder()
                        .trackName(trackName)
                        .artistName(artistName)
                        .filePath(downloadedFile.getAbsolutePath())
                        .success(true)
                        .build();
                
            } catch (IOException | InterruptedException e) {
                log.error("Error in download process", e);
                return DownloadResult.builder()
                        .trackName(trackName)
                        .artistName(artistName)
                        .success(false)
                        .errorMessage("Error: " + e.getMessage())
                        .build();
            }
        });
    }
} 