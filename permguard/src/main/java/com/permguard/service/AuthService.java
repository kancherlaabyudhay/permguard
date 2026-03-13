package com.permguard.service;

import com.permguard.*;
import com.permguard.dto.LoginResponse;
import com.permguard.dto.LoginRequest;
import com.permguard.entity.User;
import com.permguard.repository.UserRepository;
import com.permguard.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// ================================================================
//  AuthService — handles login and password change
//  ✅ FIX: removed @RequiredArgsConstructor, using explicit constructor
//          so Spring properly injects all dependencies
// ================================================================

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    // ✅ FIX: explicit constructor instead of @RequiredArgsConstructor
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    private static final int MAX_FAILED_ATTEMPTS = 5;

    // ----------------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------------
    @Transactional
    public LoginResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Your account has been deactivated. Contact admin.");
        }

        // 3. Check if account is locked
        if (user.getAccountLocked()) {
            throw new RuntimeException(
                "Account locked due to too many failed attempts. Contact admin.");
        }

        // 4. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            userRepository.incrementFailedAttempts(request.getEmail());

            int attempts = user.getFailedAttempts() + 1;

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                userRepository.lockAccount(request.getEmail());
                throw new RuntimeException(
                    "Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts. Contact admin.");
            }

            throw new RuntimeException(
                "Invalid email or password. " + (MAX_FAILED_ATTEMPTS - attempts) + " attempts remaining.");
        }

        // 5. Reset failed attempts on success
        userRepository.resetFailedAttempts(request.getEmail());

        // 6. Update last login time
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 7. Generate JWT token
        String token = jwtUtil.generateToken(user);

        // 8. Return response
        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .rollNumber(user.getRollNumber())
                .mustChangePassword(user.getMustChangePassword())
                .message(user.getMustChangePassword()
                        ? "Login successful. Please change your password."
                        : "Login successful.")
                .build();
    }

    // ----------------------------------------------------------------
    // CHANGE PASSWORD
    // ----------------------------------------------------------------
    @Transactional
    public String changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return "Password changed successfully";
    }
}