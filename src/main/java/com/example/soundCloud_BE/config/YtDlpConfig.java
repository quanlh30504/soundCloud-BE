package com.example.soundCloud_BE.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Slf4j
@Getter
public class YtDlpConfig {

    @Value("${ytdlp.auto-install:true}")
    private boolean autoInstall;
    
    @Value("${ytdlp.executable-path:}")
    private String configuredExecutablePath;
    
    private String executablePath;

    @PostConstruct
    public void init() {
        // If user has configured a path, use that
        if (configuredExecutablePath != null && !configuredExecutablePath.isEmpty()) {
            Path path = Paths.get(configuredExecutablePath);
            if (Files.exists(path)) {
                executablePath = path.toString();
                log.info("Using configured yt-dlp path: {}", executablePath);
                return;
            } else {
                log.warn("Configured yt-dlp path does not exist: {}", configuredExecutablePath);
            }
        }
        
        // Check if yt-dlp is in PATH
        String systemPath = findYtDlpInPath();
        if (systemPath != null) {
            executablePath = systemPath;
            log.info("Found yt-dlp in PATH: {}", executablePath);
            return;
        }

        // Auto install if enabled
        if (!autoInstall) {
            log.warn("yt-dlp is not installed and auto-install is disabled. Please install it manually.");
            return;
        }

        // Try to install yt-dlp
        try {
            executablePath = installYtDlp();
            log.info("Installed yt-dlp at: {}", executablePath);
        } catch (Exception e) {
            log.error("Failed to install yt-dlp. Please install it manually.", e);
        }
    }
    
    private String findYtDlpInPath() {
        try {
            String command = System.getProperty("os.name").toLowerCase().contains("win") ? "where yt-dlp" : "which yt-dlp";
            Process process = Runtime.getRuntime().exec(command);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                int exitCode = process.waitFor();
                
                if (exitCode == 0 && line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            }
            
            return null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private boolean isYtDlpInstalled(String path) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(path, "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String version = reader.readLine();
                int exitCode = process.waitFor();
                
                if (exitCode == 0 && version != null && !version.trim().isEmpty()) {
                    log.info("yt-dlp version: {}", version);
                    return true;
                }
            }
            
            return false;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String installYtDlp() throws IOException, InterruptedException {
        log.info("Attempting to install yt-dlp...");
        
        String os = System.getProperty("os.name").toLowerCase();
        Path ytDlpPath;
        
        if (os.contains("win")) {
            // Windows
            ytDlpPath = Paths.get(System.getProperty("user.home"), "yt-dlp", "yt-dlp.exe");
            Files.createDirectories(ytDlpPath.getParent());
            
            // Download yt-dlp
            log.info("Downloading yt-dlp for Windows...");
            ProcessBuilder downloadProcess = new ProcessBuilder(
                    "powershell", "-Command", 
                    "Invoke-WebRequest -Uri https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe -OutFile " + 
                    ytDlpPath.toString());
            downloadProcess.redirectErrorStream(true);
            Process process = downloadProcess.start();
            
            // Log output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Download output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to download yt-dlp.exe, exit code: " + exitCode);
            }
            
        } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
            // MacOS or Linux
            ytDlpPath = Paths.get(System.getProperty("user.home"), ".local", "bin", "yt-dlp");
            Files.createDirectories(ytDlpPath.getParent());
            
            // Download yt-dlp
            log.info("Downloading yt-dlp for Unix...");
            ProcessBuilder downloadProcess = new ProcessBuilder(
                    "curl", "-L", 
                    "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp",
                    "-o", ytDlpPath.toString());
            Process process = downloadProcess.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("Failed to download yt-dlp");
            }
            
            // Make executable
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            
            try {
                Files.setPosixFilePermissions(ytDlpPath, permissions);
            } catch (UnsupportedOperationException e) {
                // If POSIX permissions aren't supported, try chmod
                ProcessBuilder chmodProcess = new ProcessBuilder("chmod", "755", ytDlpPath.toString());
                chmodProcess.start().waitFor();
            }
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
        
        // Verify installation
        if (isYtDlpInstalled(ytDlpPath.toString())) {
            log.info("yt-dlp has been installed successfully at {}", ytDlpPath);
            return ytDlpPath.toString();
        } else {
            throw new IOException("Failed to verify yt-dlp installation");
        }
    }
    
    @Bean
    public String ytDlpExecutablePath() {
        return executablePath;
    }
} 