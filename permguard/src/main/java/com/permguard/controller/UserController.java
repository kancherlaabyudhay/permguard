
package com.permguard.controller;

import com.permguard.dto.CreateUserRequest;

import com.permguard.dto.UserResponse;
import com.permguard.entity.Department;
import com.permguard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ================================================================
//  UserController — Admin manages users
//
//  POST   /admin/users              → create student/faculty/security
//  GET    /admin/users/students     → list all students
//  GET    /admin/users/faculty      → list all faculty
//  GET    /admin/users/{id}         → get user by ID
//  PUT    /admin/users/{id}/toggle  → activate/deactivate user
//  PUT    /admin/users/{id}/unlock  → unlock locked account
//  PUT    /admin/users/{id}/reset-password → reset to default password
//  GET    /admin/departments        → list all departments
// ================================================================

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ----------------------------------------------------------------
    // POST /admin/users — Create new user
    // Body: { fullName, email, phone, role, deptId, yearOfStudy, rollNumber }
    // ----------------------------------------------------------------
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // GET /admin/users/students
    // ----------------------------------------------------------------
    @GetMapping("/users/students")
    public ResponseEntity<List<UserResponse>> getAllStudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }

    // ----------------------------------------------------------------
    // GET /admin/users/faculty
    // ----------------------------------------------------------------
    @GetMapping("/users/faculty")
    public ResponseEntity<List<UserResponse>> getAllFaculty() {
        return ResponseEntity.ok(userService.getAllFaculty());
    }

    // ----------------------------------------------------------------
    // GET /admin/users/{id}
    // ----------------------------------------------------------------
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // PUT /admin/users/{id}/toggle — Activate or Deactivate
    // ----------------------------------------------------------------
    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.toggleUserStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // PUT /admin/users/{id}/unlock — Unlock locked account
    // ----------------------------------------------------------------
    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<?> unlockAccount(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.unlockAccount(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // PUT /admin/users/{id}/reset-password — Reset to default password
    // ----------------------------------------------------------------
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            String message = userService.resetPassword(id);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // GET /admin/departments — List all departments
    // ----------------------------------------------------------------
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(userService.getAllDepartments());
    }
}
