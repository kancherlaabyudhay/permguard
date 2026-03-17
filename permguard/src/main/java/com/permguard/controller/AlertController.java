package com.permguard.controller;

import com.permguard.entity.Alert;
import com.permguard.repository.AlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepo;

    public AlertController(AlertRepository alertRepo) {
        this.alertRepo = alertRepo;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY')")
    public ResponseEntity<?> getOpenAlerts() {
        List<Map<String, Object>> result = alertRepo
                .findByResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY')")
    public ResponseEntity<?> getStudentAlerts(@PathVariable Long studentId) {
        List<Map<String, Object>> result = alertRepo
                .findByStudent_UserIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id) {
        return alertRepo.findById(id).map(alert -> {
            alert.setResolved(true);
            alertRepo.save(alert);
            return ResponseEntity.ok(Map.of("message", "Alert resolved", "alertId", id));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Alert not found")));
    }

    private Map<String, Object> toMap(Alert a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",          a.getId());
        map.put("riskLevel",   a.getRiskLevel().name());
        map.put("message",     a.getMessage());
        map.put("resolved",    a.getResolved());
        map.put("createdAt",   a.getCreatedAt());
        map.put("studentId",   a.getStudent().getUserId());
        map.put("studentName", a.getStudent().getFullName());
        map.put("rollNumber",  a.getStudent().getRollNumber());
        return map;
    }
}
