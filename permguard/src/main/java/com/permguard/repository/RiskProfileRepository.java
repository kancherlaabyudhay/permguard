package com.permguard.repository;

import com.permguard.entity.RiskProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, Long> {

    Optional<RiskProfile> findByStudent_UserId(Long studentId);

    // Top N riskiest students — used for admin dashboard
    @Query("SELECT rp FROM RiskProfile rp ORDER BY rp.currentScore DESC")
    List<RiskProfile> findTopRiskyStudents();
}