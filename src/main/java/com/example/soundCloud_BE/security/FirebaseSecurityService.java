package com.example.soundCloud_BE.security;

// src/main/java/com/musicapp/security/FirebaseSecurityService.java
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class FirebaseSecurityService {

    // Lấy Firebase UID từ SecurityContext
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
    }

    // Xác thực token và trả về FirebaseToken
    public FirebaseToken verifyToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid ID token");
        }
    }

    // Lấy thông tin user từ token
    public FirebaseUserDetails getUserDetails(String idToken) {
        FirebaseToken decodedToken = verifyToken(idToken);
        return new FirebaseUserDetails(
                decodedToken.getUid(),
                decodedToken.getEmail(),
                decodedToken.getName(),
                decodedToken.getPicture()
        );
    }

    // Record chứa thông tin user
    public record FirebaseUserDetails(
            String uid,
            String email,
            String displayName,
            String avatarUrl
    ) {}
}
