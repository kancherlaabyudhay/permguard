package com.permguard.repository;

import com.permguard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findAllByOrderByCreatedAtDesc();

    long countByResolved(boolean resolved);

    long countByRiskLevel(Alert.RiskLevel riskLevel);

    List<Alert> findByStudent_UserIdOrderByCreatedAtDesc(Long studentId);
}