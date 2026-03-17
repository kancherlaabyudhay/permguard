package com.permguard.service;

import com.permguard.entity.Alert;
import com.permguard.entity.Permission;
import com.permguard.entity.RiskProfile;
import com.permguard.entity.User;
import com.permguard.repository.AlertRepository;
import com.permguard.repository.PermissionRepository;
import com.permguard.repository.RiskProfileRepository;
import com.permguard.repository.UsageLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiryScheduler {

    private final PermissionRepository permissionRepository;
    private final UsageLogRepository   usageLogRepository;
    private final AlertRepository      alertRepository;
    private final RiskProfileRepository riskProfileRepository;

    public ExpiryScheduler(PermissionRepository permissionRepository,
                           UsageLogRepository usageLogRepository,
                           AlertRepository alertRepository,
                           RiskProfileRepository riskProfileRepository) {
        this.permissionRepository  = permissionRepository;
        this.usageLogRepository    = usageLogRepository;
        this.alertRepository       = alertRepository;
        this.riskProfileRepository = riskProfileRepository;
    }

    @Scheduled(fixedRate = 60000) // every 60 seconds
    @Transactional
    public void checkExpiredPermissions() {
        // Find all APPROVED permissions that have passed expiry time
        List<Permission> expired = permissionRepository
                .findExpiredApprovedPermissions(LocalDateTime.now());

        for (Permission permission : expired) {
            // Mark as EXPIRED
            permission.setStatus(Permission.Status.EXPIRED);
            permissionRepository.save(permission);

            // Check if student exited but never returned
            long exitCount   = usageLogRepository.countExitScans(permission.getId());
            long returnCount = usageLogRepository.countReturnScans(permission.getId());

            if (exitCount > 0 && returnCount == 0) {
                // Student left but never came back — generate HIGH fraud alert
                generateTimeoutAlert(permission);
            }
        }
    }

    private void generateTimeoutAlert(Permission permission) {
        User student = permission.getStudent();

        // Check if alert already exists for this permission
        List<Alert> existing = alertRepository
                .findByStudent_UserIdOrderByCreatedAtDesc(student.getUserId());

        boolean alreadyAlerted = existing.stream().anyMatch(a ->
                !a.getResolved() &&
                a.getMessage() != null &&
                a.getMessage().contains("permission #" + permission.getId()));

        if (alreadyAlerted) return; // don't duplicate alerts

        // Create fraud alert
        Alert alert = new Alert();
        alert.setStudent(student);
        alert.setRiskLevel(Alert.RiskLevel.HIGH);
        alert.setMessage("⚠️ Student did not return to campus! " +
                "Permission #" + permission.getId() +
                " (" + permission.getType().replace("_", " ") + ") " +
                "expired at " + permission.getExpiryTime() +
                " but no RETURN scan was recorded.");
        alert.setResolved(false);
        alertRepository.save(alert);

        // Update risk profile score
        updateRiskProfile(student, 40); // +40 points for no-return
    }

    private void updateRiskProfile(User student, double scoreIncrease) {
        try {
            RiskProfile profile = riskProfileRepository
                    .findByStudent_UserId(student.getUserId())
                    .orElseGet(() -> {
                        RiskProfile p = new RiskProfile();
                        p.setStudent(student);
                        p.setCurrentScore(0.0);
                        p.setTotalTimeouts(0);
                        p.setTotalOverstays(0);
                        p.setTotalOffHourExits(0);
                        return p;
                    });

            profile.setCurrentScore(
                    Math.min(100.0, profile.getCurrentScore() + scoreIncrease));
            profile.setTotalTimeouts(
                    (profile.getTotalTimeouts() != null ? profile.getTotalTimeouts() : 0) + 1);
            profile.setUpdatedAt(LocalDateTime.now());
            riskProfileRepository.save(profile);
        } catch (Exception e) {
            System.err.println("[ExpiryScheduler] Risk profile update failed: " + e.getMessage());
        }
    }
}
