package com.permguard.service;

import com.permguard.dto.PermissionRequest;
import com.permguard.dto.PermissionResponse;
import com.permguard.entity.Permission;
import com.permguard.entity.User;
import com.permguard.repository.PermissionRepository;
import com.permguard.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository       userRepository;
    private final QrCodeService        qrCodeService;

    public PermissionService(PermissionRepository permissionRepository,
                             UserRepository userRepository,
                             @Lazy QrCodeService qrCodeService) {
        this.permissionRepository = permissionRepository;
        this.userRepository       = userRepository;
        this.qrCodeService        = qrCodeService;
    }

    @Transactional
    public PermissionResponse submitRequest(String studentEmail,
                                            PermissionRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        long pendingCount = permissionRepository
                .countByStudentAndStatus(student, Permission.Status.PENDING);
        if (pendingCount > 0) {
            throw new RuntimeException("You already have a pending permission request.");
        }

        LocalDateTime expiryTime = LocalDateTime.parse(request.getExpiryTime());
        if (expiryTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Expiry time must be in the future");
        }

        // facultyId optional — auto-assign to first faculty if not provided
        User faculty = null;
        if (request.getFacultyId() != null) {
            faculty = userRepository.findById(request.getFacultyId()).orElse(null);
        }
        if (faculty == null) {
            faculty = userRepository.findFirstByRole(User.Role.FACULTY).orElse(null);
        }

        Permission permission = new Permission();
        permission.setStudent(student);
        permission.setFaculty(faculty);
        permission.setType(request.getType());
        permission.setReason(request.getReason());
        permission.setExpiryTime(expiryTime);
        permission.setStatus(Permission.Status.PENDING);

        Permission saved = permissionRepository.save(permission);
        return PermissionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getMyPermissions(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return permissionRepository
                .findByStudentOrderByCreatedAtDesc(student)
                .stream().map(PermissionResponse::from).collect(Collectors.toList());
    }

    // ✅ FIX: Faculty sees ALL pending permissions, not just assigned ones
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPendingForFaculty(String facultyEmail) {
        return permissionRepository
                .findByStatusOrderByCreatedAtDesc(Permission.Status.PENDING)
                .stream().map(PermissionResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public PermissionResponse approvePermission(Long permissionId, String approverEmail) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (permission.getStatus() != Permission.Status.PENDING) {
            throw new RuntimeException("Permission is not in PENDING state: " + permission.getStatus());
        }

        // Set approving faculty if not already set
        if (permission.getFaculty() == null) {
            userRepository.findByEmail(approverEmail).ifPresent(permission::setFaculty);
        }

        permission.setStatus(Permission.Status.APPROVED);
        permission.setApprovedAt(LocalDateTime.now());
        Permission saved = permissionRepository.save(permission);

        try {
            qrCodeService.generateQrForPermission(saved.getId());
        } catch (Exception e) {
            System.err.println("[QR] Failed to auto-generate QR: " + e.getMessage());
        }

        return PermissionResponse.from(saved);
    }

    @Transactional
    public PermissionResponse rejectPermission(Long permissionId, String rejectorEmail) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (permission.getStatus() != Permission.Status.PENDING) {
            throw new RuntimeException("Permission is not in PENDING state: " + permission.getStatus());
        }

        permission.setStatus(Permission.Status.REJECTED);
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    // ✅ FIX: Admin sees ALL permissions not just pending
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository
                .findAllByOrderByCreatedAtDesc()
                .stream().map(PermissionResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionResponse getById(Long id) {
        return PermissionResponse.from(permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + id)));
    }
}