package com.permguard.scheduler;

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
public class PermissionExpiryScheduler {

    private final PermissionRepository  permissionRepository;
    private final UsageLogRepository    usageLogRepository;
    private final AlertRepository       alertRepository;
    private final RiskProfileRepository riskProfileRepository;

    public PermissionExpiryScheduler(PermissionRepository permissionRepository,
                                     UsageLogRepository usageLogRepository,
                                     AlertRepository alertRepository,
                                     RiskProfileRepository riskProfileRepository) {
        this.permissionRepository  = permissionRepository;
        this.usageLogRepository    = usageLogRepository;
        this.alertRepository       = alertRepository;
        this.riskProfileRepository = riskProfileRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePermissionsAndFlagTimeouts() {
        List<Permission> expired = permissionRepository
                .findExpiredApprovedPermissions(LocalDateTime.now());

        for (Permission p : expired) {
            p.setStatus(Permission.Status.EXPIRED);
            permissionRepository.save(p);

            // Check if student exited but never returned
            long exitCount   = usageLogRepository.countExitScans(p.getId());
            long returnCount = usageLogRepository.countReturnScans(p.getId());

            if (exitCount > 0 && returnCount == 0) {
                generateTimeoutAlert(p);
            }
        }

        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Expired " + expired.size() + " permissions.");
        }
    }

    private void generateTimeoutAlert(Permission permission) {
        User student = permission.getStudent();

        // Avoid duplicate alerts for same permission
        boolean alreadyAlerted = alertRepository
                .findByStudent_UserIdOrderByCreatedAtDesc(student.getUserId())
                .stream()
                .anyMatch(a -> !a.getResolved() &&
                        a.getMessage() != null &&
                        a.getMessage().contains("Permission #" + permission.getId()));

        if (alreadyAlerted) return;

        // Create HIGH fraud alert
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

        // Update risk profile
        updateRiskProfile(student);

        System.out.println("[Scheduler] Fraud alert created for student: "
                + student.getFullName() + " permission #" + permission.getId());
    }

    private void updateRiskProfile(User student) {
        try {
            RiskProfile profile = riskProfileRepository
                    .findByStudent_UserId(student.getUserId())
                    .orElseGet(() -> {
                        RiskProfile p = new RiskProfile(student);
                        p.setCurrentScore(0.0);
                        p.setTotalTimeouts(0);
                        p.setTotalOverstays(0);
                        p.setTotalOffHourExits(0);
                        return p;
                    });

            // +40 risk points for no-return timeout
            profile.setCurrentScore(Math.min(100.0, profile.getCurrentScore() + 40.0));
            profile.setTotalTimeouts(profile.getTotalTimeouts() + 1);
            riskProfileRepository.save(profile);
        } catch (Exception e) {
            System.err.println("[Scheduler] Risk profile update failed: " + e.getMessage());
        }
    }
}
