package com.permguard.controller;

import com.permguard.service.FraudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ================================================================
//  FraudController
//
//  GET  /fraud/alerts              → all unresolved alerts
//  GET  /fraud/alerts/all          → all alerts (including resolved)
//  POST /fraud/alerts/{id}/resolve → mark alert as resolved
//  GET  /fraud/risk-profiles       → top risky students
//  GET  /fraud/risk-profiles/{id}  → risk profile for one student
//  GET  /fraud/dashboard           → full fraud summary
// ================================================================

@RestController
@RequestMapping("/fraud")
public class FraudController {

    private final FraudService fraudService;

    public FraudController(FraudService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getUnresolvedAlerts() {
        return ResponseEntity.ok(fraudService.getUnresolvedAlerts());
    }

    @GetMapping("/alerts/all")
    public ResponseEntity<?> getAllAlerts() {
        return ResponseEntity.ok(fraudService.getAllAlerts());
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(fraudService.resolveAlert(id));
    }

    @GetMapping("/risk-profiles")
    public ResponseEntity<?> getRiskProfiles() {
        return ResponseEntity.ok(fraudService.getTopRiskyStudents());
    }

    @GetMapping("/risk-profiles/{studentId}")
    public ResponseEntity<?> getStudentRiskProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(fraudService.getStudentRiskProfile(studentId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getFraudDashboard() {
        return ResponseEntity.ok(fraudService.getFraudDashboard());
    }
}