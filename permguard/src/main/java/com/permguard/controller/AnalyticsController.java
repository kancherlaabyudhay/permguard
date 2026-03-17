package com.permguard.controller;

import com.permguard.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ================================================================
//  AnalyticsController
//
//  GET /analytics/summary      → KPI cards (totals, rates)
//  GET /analytics/by-type      → permissions grouped by type
//  GET /analytics/by-status    → permissions grouped by status
//  GET /analytics/by-dept      → permissions grouped by department
//  GET /analytics/daily-trend  → daily count for last 30 days
//  GET /analytics/top-students → students with most requests
//  GET /analytics/recent-scans → last 20 gate scans
//  GET /analytics/report       → full combined report
// ================================================================

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY', 'ROLE_STUDENT', 'ROLE_SECURITY')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/by-type")
    public ResponseEntity<?> byType() {
        return ResponseEntity.ok(analyticsService.getPermissionsByType());
    }

    @GetMapping("/by-status")
    public ResponseEntity<?> byStatus() {
        return ResponseEntity.ok(analyticsService.getPermissionsByStatus());
    }

    @GetMapping("/by-dept")
    public ResponseEntity<?> byDept() {
        return ResponseEntity.ok(analyticsService.getPermissionsByDepartment());
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<?> dailyTrend() {
        return ResponseEntity.ok(analyticsService.getDailyTrend());
    }

    @GetMapping("/top-students")
    public ResponseEntity<?> topStudents(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopStudents(limit));
    }

    @GetMapping("/recent-scans")
    public ResponseEntity<?> recentScans() {
        return ResponseEntity.ok(analyticsService.getRecentScans());
    }

    @GetMapping("/report")
    public ResponseEntity<?> fullReport() {
        return ResponseEntity.ok(analyticsService.getFullReport());
    }
}
