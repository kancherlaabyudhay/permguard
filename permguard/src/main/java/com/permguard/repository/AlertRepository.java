package com.permguard.repository;

import com.permguard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT a FROM Alert a JOIN FETCH a.student WHERE a.resolved = false ORDER BY a.createdAt DESC")
    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

    @Query("SELECT a FROM Alert a JOIN FETCH a.student ORDER BY a.createdAt DESC")
    List<Alert> findAllByOrderByCreatedAtDesc();

    long countByResolved(boolean resolved);

    long countByRiskLevel(Alert.RiskLevel riskLevel);

    @Query("SELECT a FROM Alert a JOIN FETCH a.student WHERE a.student.userId = :studentId ORDER BY a.createdAt DESC")
    List<Alert> findByStudent_UserIdOrderByCreatedAtDesc(@Param("studentId") Long studentId);
}
