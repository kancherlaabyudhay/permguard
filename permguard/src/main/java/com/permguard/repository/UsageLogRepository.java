package com.permguard.repository;

import com.permguard.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    // Used by AnalyticsService
    List<UsageLog> findTop100ByOrderByScannedAtDesc();

    // Used by GateScanService — scan history for a permission
    List<UsageLog> findByPermission_IdOrderByScannedAtDesc(Long permissionId);

    // Used by RiskScoringService — count exits by student in a time window
    @Query("""
        SELECT COUNT(u)
        FROM UsageLog u
        WHERE u.permission.student.userId = :studentId
          AND u.scanType = 'EXIT'
          AND u.scannedAt BETWEEN :from AND :to
        """)
    long countExitsByStudentInWindow(
            @Param("studentId") Long studentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
