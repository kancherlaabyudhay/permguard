
package com.permguard.service;

import com.permguard.dto.CreateUserRequest;

import com.permguard.dto.UserResponse;
import com.permguard.entity.Department;
import com.permguard.entity.User;
import com.permguard.repository.DepartmentRepository;
import com.permguard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ================================================================
//  UserService — Admin creates/manages students, faculty, security
// ================================================================

@Service
public class UserService {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;

    @Value("${app.default-password-prefix}")
    private String defaultPasswordPrefix;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository       = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder      = passwordEncoder;
    }

    // ----------------------------------------------------------------
    // CREATE USER (Admin only)
    // Default password = PERM@ + first part of email
    // e.g. email: john@mru.com → password: PERM@john
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // 1. Check email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // 2. Check roll number already exists (for students)
        if (request.getRollNumber() != null &&
            userRepository.existsByRollNumber(request.getRollNumber())) {
            throw new RuntimeException("Roll number already exists: " + request.getRollNumber());
        }

        // 3. Find department if provided
        Department department = null;
        if (request.getDeptId() != null) {
            department = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + request.getDeptId()));
        }

        // 4. Generate default password
        String emailPrefix     = request.getEmail().split("@")[0];
        String defaultPassword = defaultPasswordPrefix + emailPrefix;
        String hashedPassword  = passwordEncoder.encode(defaultPassword);

        // 5. Build user entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(hashedPassword);
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
        user.setDepartment(department);
        user.setYearOfStudy(request.getYearOfStudy());
        user.setRollNumber(request.getRollNumber());
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        user.setMustChangePassword(true);  // must change on first login

        // 6. Save
        User saved = userRepository.save(user);

        // 7. TODO: Send email with default password (email module later)
        System.out.println("==================================");
        System.out.println("New user created: " + saved.getEmail());
        System.out.println("Default password: " + defaultPassword);
        System.out.println("==================================");

        return UserResponse.from(saved);
    }

    // ----------------------------------------------------------------
    // GET ALL USERS BY ROLE
    // ----------------------------------------------------------------
    public List<UserResponse> getUsersByRole(String role) {
        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        return userRepository.findByRole(userRole)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // GET ALL STUDENTS
    // ----------------------------------------------------------------
    public List<UserResponse> getAllStudents() {
        return getUsersByRole("STUDENT");
    }

    // ----------------------------------------------------------------
    // GET ALL FACULTY
    // ----------------------------------------------------------------
    public List<UserResponse> getAllFaculty() {
        return getUsersByRole("FACULTY");
    }

    // ----------------------------------------------------------------
    // GET USER BY ID
    // ----------------------------------------------------------------
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return UserResponse.from(user);
    }

    // ----------------------------------------------------------------
    // TOGGLE ACTIVE STATUS (Admin activates/deactivates user)
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setIsActive(!user.getIsActive());
        return UserResponse.from(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // UNLOCK ACCOUNT (Admin unlocks after too many failed attempts)
    // ----------------------------------------------------------------
    @Transactional
    public UserResponse unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        return UserResponse.from(userRepository.save(user));
    }

    // ----------------------------------------------------------------
    // RESET PASSWORD (Admin resets to default password)
    // ----------------------------------------------------------------
    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String emailPrefix     = user.getEmail().split("@")[0];
        String defaultPassword = defaultPasswordPrefix + emailPrefix;

        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        return "Password reset. New default password: " + defaultPassword;
    }

    // ----------------------------------------------------------------
    // GET ALL DEPARTMENTS
    // ----------------------------------------------------------------
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
