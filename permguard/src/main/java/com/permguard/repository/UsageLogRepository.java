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

    @Query("SELECT u FROM UsageLog u LEFT JOIN FETCH u.permission p LEFT JOIN FETCH p.student ORDER BY u.scannedAt DESC")
    List<UsageLog> findRecentScansWithDetails();

    List<UsageLog> findTop100ByOrderByScannedAtDesc();

    List<UsageLog> findByPermission_IdOrderByScannedAtDesc(Long permissionId);

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

    @Query("SELECT COUNT(u) FROM UsageLog u WHERE u.permission.id = :permissionId AND u.scanType = 'EXIT' AND u.outcome = 'ALLOWED'")
    long countExitScans(@Param("permissionId") Long permissionId);

    @Query("SELECT COUNT(u) FROM UsageLog u WHERE u.permission.id = :permissionId AND u.scanType = 'RETURN' AND u.outcome = 'ALLOWED'")
    long countReturnScans(@Param("permissionId") Long permissionId);
}
