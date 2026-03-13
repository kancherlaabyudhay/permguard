package com.permguard.service;

import com.permguard.entity.Permission;

import com.permguard.entity.User;
import com.permguard.repository.PermissionRepository;
import com.permguard.repository.UsageLogRepository;
import com.permguard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final PermissionRepository permissionRepository;
    private final UsageLogRepository   usageLogRepository;
    private final UserRepository       userRepository;

    public AnalyticsService(PermissionRepository permissionRepository,
                            UsageLogRepository usageLogRepository,
                            UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.usageLogRepository   = usageLogRepository;
        this.userRepository       = userRepository;
    }

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        long total    = permissionRepository.count();
        long pending  = permissionRepository.countByStatus(Permission.Status.PENDING);
        long approved = permissionRepository.countByStatus(Permission.Status.APPROVED);
        long rejected = permissionRepository.countByStatus(Permission.Status.REJECTED);
        long expired  = permissionRepository.countByStatus(Permission.Status.EXPIRED);
        long flagged  = permissionRepository.countByStatus(Permission.Status.FLAGGED);
        long totalScans    = usageLogRepository.count();
        // ✅ FIX: use enum, not String
        long totalStudents = userRepository.countByRole(User.Role.STUDENT);

        summary.put("totalPermissions",   total);
        summary.put("pendingPermissions",  pending);
        summary.put("approvedPermissions", approved);
        summary.put("rejectedPermissions", rejected);
        summary.put("expiredPermissions",  expired);
        summary.put("flaggedPermissions",  flagged);
        summary.put("totalGateScans",      totalScans);
        summary.put("totalStudents",       totalStudents);
        double approvalRate = total > 0
                ? Math.round((approved * 100.0 / total) * 10.0) / 10.0 : 0;
        summary.put("approvalRate", approvalRate);
        return summary;
    }

    public List<Map<String, Object>> getPermissionsByType() {
        return permissionRepository.countGroupByType().stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type",  row[0]);
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPermissionsByStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Permission.Status status : Permission.Status.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", status.name());
            item.put("count",  permissionRepository.countByStatus(status));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getPermissionsByDepartment() {
        return permissionRepository.countGroupByDepartment().stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("department", row[0] != null ? row[0] : "Unknown");
            item.put("count",      row[1]);
            return item;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getDailyTrend() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        return permissionRepository.countGroupByDate(from).stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date",  row[0] != null ? row[0].toString() : "");
            item.put("count", row[1]);
            return item;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopStudents(int limit) {
        return permissionRepository.topStudentsByPermissions().stream().limit(limit).map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentName", row[0]);
            item.put("rollNumber",  row[1]);
            item.put("count",       row[2]);
            return item;
        }).collect(Collectors.toList());
    }

    public Object getRecentScans() {
        return usageLogRepository.findTop100ByOrderByScannedAtDesc()
                .stream().limit(20).collect(Collectors.toList());
    }

    public Map<String, Object> getFullReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary",      getDashboardSummary());
        report.put("byType",       getPermissionsByType());
        report.put("byStatus",     getPermissionsByStatus());
        report.put("byDepartment", getPermissionsByDepartment());
        report.put("dailyTrend",   getDailyTrend());
        report.put("topStudents",  getTopStudents(10));
        report.put("generatedAt",  LocalDateTime.now().toString());
        return report;
    }
}