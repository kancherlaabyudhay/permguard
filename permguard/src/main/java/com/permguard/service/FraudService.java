package com.permguard.service;

import com.permguard.entity.Alert;
import com.permguard.entity.RiskProfile;
import com.permguard.repository.AlertRepository;
import com.permguard.repository.RiskProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// ================================================================
//  FraudService — exposes fraud/alert data to the dashboard
// ================================================================

@Service
public class FraudService {

    private final AlertRepository       alertRepo;
    private final RiskProfileRepository riskProfileRepo;

    public FraudService(AlertRepository alertRepo,
                        RiskProfileRepository riskProfileRepo) {
        this.alertRepo       = alertRepo;
        this.riskProfileRepo = riskProfileRepo;
    }

    // ----------------------------------------------------------------
    // ALERTS
    // ----------------------------------------------------------------

    public List<Map<String, Object>> getUnresolvedAlerts() {
        return alertRepo.findByResolvedFalseOrderByCreatedAtDesc()
                .stream().map(this::mapAlert).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllAlerts() {
        return alertRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapAlert).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> resolveAlert(Long alertId) {
        Alert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setResolved(true);
        alertRepo.save(alert);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message",  "Alert resolved successfully");
        result.put("alertId",  alertId);
        result.put("resolved", true);
        return result;
    }

    // ----------------------------------------------------------------
    // RISK PROFILES
    // ----------------------------------------------------------------

    public List<Map<String, Object>> getTopRiskyStudents() {
        return riskProfileRepo.findTopRiskyStudents()
                .stream().map(this::mapRiskProfile).collect(Collectors.toList());
    }

    public Map<String, Object> getStudentRiskProfile(Long studentId) {
        RiskProfile profile = riskProfileRepo.findByStudent_UserId(studentId)
                .orElseThrow(() -> new RuntimeException("No risk profile found for student: " + studentId));
        return mapRiskProfile(profile);
    }

    // ----------------------------------------------------------------
    // FRAUD DASHBOARD — summary for admin
    // ----------------------------------------------------------------

    public Map<String, Object> getFraudDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        long totalAlerts      = alertRepo.count();
        long unresolvedAlerts = alertRepo.countByResolved(false);
        long highRiskAlerts   = alertRepo.countByRiskLevel(Alert.RiskLevel.HIGH);
        long mediumRiskAlerts = alertRepo.countByRiskLevel(Alert.RiskLevel.MEDIUM);

        // Students with score > 70 (high risk)
        long highRiskStudents = riskProfileRepo.findTopRiskyStudents()
                .stream().filter(p -> p.getCurrentScore() >= 70).count();

        // Students with score > 40 (medium risk)
        long mediumRiskStudents = riskProfileRepo.findTopRiskyStudents()
                .stream().filter(p -> p.getCurrentScore() >= 40 && p.getCurrentScore() < 70).count();

        dashboard.put("totalAlerts",        totalAlerts);
        dashboard.put("unresolvedAlerts",   unresolvedAlerts);
        dashboard.put("highRiskAlerts",     highRiskAlerts);
        dashboard.put("mediumRiskAlerts",   mediumRiskAlerts);
        dashboard.put("highRiskStudents",   highRiskStudents);
        dashboard.put("mediumRiskStudents", mediumRiskStudents);
        dashboard.put("topRiskyStudents",   getTopRiskyStudents().stream().limit(5).collect(Collectors.toList()));
        dashboard.put("recentAlerts",       getUnresolvedAlerts().stream().limit(10).collect(Collectors.toList()));

        return dashboard;
    }

    // ----------------------------------------------------------------
    // Mappers
    // ----------------------------------------------------------------

    private Map<String, Object> mapAlert(Alert alert) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("alertId",      alert.getId());
        map.put("studentName",  alert.getStudent().getFullName());
        map.put("studentRoll",  alert.getStudent().getRollNumber());
        map.put("riskLevel",    alert.getRiskLevel().name());
        map.put("message",      alert.getMessage());
        map.put("resolved",     alert.getResolved());
        map.put("createdAt",    alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null);
        return map;
    }

    private Map<String, Object> mapRiskProfile(RiskProfile profile) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("studentId",         profile.getStudent().getUserId());
        map.put("studentName",       profile.getStudent().getFullName());
        map.put("studentRoll",       profile.getStudent().getRollNumber());
        map.put("currentScore",      profile.getCurrentScore());
        map.put("riskLevel",         getRiskLevel(profile.getCurrentScore()));
        map.put("totalTimeouts",     profile.getTotalTimeouts());
        map.put("totalOverstays",    profile.getTotalOverstays());
        map.put("totalOffHourExits", profile.getTotalOffHourExits());
        map.put("updatedAt",         profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null);
        return map;
    }

    private String getRiskLevel(double score) {
        if (score >= 90) return "CRITICAL";
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}