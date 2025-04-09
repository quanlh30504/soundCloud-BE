package com.example.soundCloud_BE.service;

import com.example.soundCloud_BE.dto.UpdateProfileRequest;
import com.example.soundCloud_BE.dto.UserResponse;
import com.example.soundCloud_BE.model.User;
import com.example.soundCloud_BE.repository.UserRepository;
import com.example.soundCloud_BE.security.FirebaseSecurityService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FirebaseSecurityService firebaseSecurityService;
    private final FirebaseAuth firebaseAuth;


    // Lấy thông tin user hiện tại
    public UserResponse getCurrentUser() {
        String uid = firebaseSecurityService.getCurrentUserId();
        User user = userRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new NullPointerException("User not found"));

        return UserResponse.builder()
                .firebaseId(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Cập nhật profile
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        String uid = firebaseSecurityService.getCurrentUserId();

        // 1. Cập nhật trên Firebase (nếu cần)
        try {
            firebaseAuth.updateUser(
                    new UserRecord.UpdateRequest(uid)
                            .setDisplayName(request.getDisplayName())
            );
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Failed to update Firebase profile");
        }

        // 2. Cập nhật database
        User user = userRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new NullPointerException("User not found"));

        user.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(user);

        return UserResponse.builder()
                .firebaseId(user.getFirebaseUid())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();

    }



}
