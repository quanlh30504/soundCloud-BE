package com.example.soundCloud_BE.controller;

import com.example.soundCloud_BE.model.User;
import com.example.soundCloud_BE.repository.UserRepository;
import com.example.soundCloud_BE.security.FirebaseSecurityService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(
            @RequestBody User userData,
            @RequestHeader("Authorization") String idToken) throws FirebaseAuthException {

        // Xác thực token
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken.replace("Bearer ", ""));

        // Kiểm tra user đã tồn tại chưa
        User user = userRepository.findByFirebaseUid(decodedToken.getUid())
                .orElse(new User());

        // Cập nhật thông tin
        user.setFirebaseUid(decodedToken.getUid());
        user.setEmail(userData.getEmail());
        user.setDisplayName(userData.getDisplayName());
        user.setAvatarUrl(userData.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}