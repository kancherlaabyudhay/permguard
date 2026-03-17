package com.permguard.service;

import com.permguard.entity.Permission;

import com.permguard.entity.UsageLog;
import com.permguard.entity.User;
import com.permguard.repository.PermissionRepository;
import com.permguard.repository.UsageLogRepository;
import com.permguard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GateScanService {

    private final PermissionRepository permissionRepository;
    private final UsageLogRepository   usageLogRepository;
    private final UserRepository       userRepository;

    public GateScanService(PermissionRepository permissionRepository,
                           UsageLogRepository usageLogRepository,
                           UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.usageLogRepository   = usageLogRepository;
        this.userRepository       = userRepository;
    }

    @Transactional
    public Map<String, Object> scanQrToken(String qrToken, String scannerEmail,
                                           String scanType) {
        Map<String, Object> result = new HashMap<>();

        Permission permission = permissionRepository.findByQrToken(qrToken).orElse(null);

        if (permission == null) {
            result.put("valid",   false);
            result.put("reason",  "INVALID_TOKEN");
            result.put("message", "QR code not recognized");
            return result;
        }

        if (permission.getStatus() == Permission.Status.EXPIRED) {
            result.put("valid",   false);
            result.put("reason",  "EXPIRED");
            result.put("message", "Permission has expired");
            logScan(permission, scannerEmail, scanType, "DENIED", "Permission expired");
            return result;
        }

        if (permission.getStatus() == Permission.Status.REJECTED) {
            result.put("valid",   false);
            result.put("reason",  "REJECTED");
            result.put("message", "Permission was rejected");
            logScan(permission, scannerEmail, scanType, "DENIED", "Permission rejected");
            return result;
        }

        if (permission.getStatus() != Permission.Status.APPROVED) {
            result.put("valid",   false);
            result.put("reason",  "NOT_APPROVED");
            result.put("message", "Permission is not approved (status: "
                    + permission.getStatus() + ")");
            logScan(permission, scannerEmail, scanType, "DENIED", "Not approved");
            return result;
        }

        if (permission.getExpiryTime().isBefore(LocalDateTime.now())) {
            permission.setStatus(Permission.Status.EXPIRED);
            permissionRepository.save(permission);
            result.put("valid",   false);
            result.put("reason",  "EXPIRED");
            result.put("message", "Permission expired at " + permission.getExpiryTime());
            logScan(permission, scannerEmail, scanType, "DENIED", "Time expired");
            return result;
        }

        logScan(permission, scannerEmail, scanType, "ALLOWED", "Valid permission");

        result.put("valid",          true);
        result.put("reason",         "APPROVED");
        result.put("message",        "✅ Access granted");
        result.put("studentName",    permission.getStudent().getFullName());
        result.put("studentRoll",    permission.getStudent().getRollNumber());
        result.put("permissionType", permission.getType());
        result.put("leaveReason",    permission.getReason());
        result.put("expiryTime",     permission.getExpiryTime().toString());
        result.put("approvedBy",     permission.getFaculty() != null
                ? permission.getFaculty().getFullName() : "Admin");
        result.put("permissionId",   permission.getId());
        return result;
    }
    // Allow RETURN even if expired - student is coming back
if ("RETURN".equalsIgnoreCase(scanType) && 
    (permission.getStatus() == Permission.Status.APPROVED || 
     permission.getStatus() == Permission.Status.EXPIRED)) {
    
    logScan(permission, scannerEmail, scanType, "ALLOWED", "Student returned to campus");
    
    result.put("valid", true);
    result.put("reason", "RETURNED");
    result.put("message", "✅ Student returned to campus");
    result.put("studentName", permission.getStudent().getFullName());
    result.put("studentRoll", permission.getStudent().getRollNumber());
    result.put("permissionType", permission.getType());
    result.put("leaveReason", permission.getReason());
    result.put("expiryTime", permission.getExpiryTime().toString());
    result.put("approvedBy", permission.getFaculty() != null 
            ? permission.getFaculty().getFullName() : "Admin");
    result.put("permissionId", permission.getId());
    return result;
}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logScan(Permission permission, String scannerEmail,
                        String scanType, String outcome, String notes) {
        try {
            User scanner = userRepository.findByEmail(scannerEmail).orElse(null);

            UsageLog log = new UsageLog();
            log.setPermission(permission);
            // ✅ FIX: scannedBy is Long, extract userId from User object
            log.setScannedBy(scanner != null ? scanner.getUserId() : null);
            log.setScanType(scanType != null ? scanType : "EXIT");
            log.setOutcome(outcome);
            log.setNotes(notes);
            log.setScannedAt(LocalDateTime.now());
            // exitTime for fraud detection — set on EXIT scans
            if ("EXIT".equalsIgnoreCase(scanType)) {
                log.setExitTime(LocalDateTime.now());
            }

            usageLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("[GateScan] Log failed: " + e.getMessage());
        }
    }

    public Object getScanHistory(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        // ✅ FIX: use correct repository method name
        return usageLogRepository.findByPermission_IdOrderByScannedAtDesc(permissionId);
    }
}
