package com.permguard.controller;

import com.permguard.dto.LoginRequest;
import com.permguard.dto.LoginResponse;
import com.permguard.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:3000"
}, allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.POST,
    RequestMethod.PUT, RequestMethod.DELETE,
    RequestMethod.OPTIONS
})
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        try {
            Long   userId  = Long.valueOf(body.get("userId").toString());
            String oldPass = body.get("oldPassword").toString();
            String newPass = body.get("newPassword").toString();
            String message = authService.changePassword(userId, oldPass, newPass);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }                                          // ← changePassword closes here

    // ✅ OUTSIDE changePassword — at class level
    @GetMapping("/generate-hash")
    public ResponseEntity<?> generateHash() {
        String hash = new BCryptPasswordEncoder(10).encode("password123");
        return ResponseEntity.ok(Map.of("hash", hash));
    }

}