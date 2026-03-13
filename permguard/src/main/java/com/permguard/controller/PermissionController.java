package com.permguard.controller;

import com.permguard.dto.PermissionRequest;
import com.permguard.dto.PermissionResponse;
import com.permguard.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<?> submitRequest(
            @Valid @RequestBody PermissionRequest request,
            Authentication auth) {
        try {
            return ResponseEntity.ok(permissionService.submitRequest(auth.getName(), request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<PermissionResponse>> getMyPermissions(Authentication auth) {
        return ResponseEntity.ok(permissionService.getMyPermissions(auth.getName()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PermissionResponse>> getPendingForFaculty(Authentication auth) {
        return ResponseEntity.ok(permissionService.getPendingForFaculty(auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    // ✅ Only @PostMapping — matches frontend api.js
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(permissionService.approvePermission(id, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Only @PostMapping — matches frontend api.js
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(permissionService.rejectPermission(id, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(permissionService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}