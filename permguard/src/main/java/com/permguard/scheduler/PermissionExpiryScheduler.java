package com.permguard.scheduler;

import com.permguard.entity.Permission;
import com.permguard.repository.PermissionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PermissionExpiryScheduler {

    private final PermissionRepository permissionRepository;

    public PermissionExpiryScheduler(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    @Transactional
    public void expirePermissionsAndFlagTimeouts() {
        List<Permission> expired = permissionRepository
                .findExpiredApprovedPermissions(LocalDateTime.now());

        for (Permission p : expired) {
            p.setStatus(Permission.Status.EXPIRED);
            permissionRepository.save(p);
        }

        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Expired " + expired.size() + " permissions.");
        }
    }
}