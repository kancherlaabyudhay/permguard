package com.permguard.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// ================================================================
//  RiskProfile — rolling fraud risk score per student
//  Maps to `risk_profiles` table
// ================================================================

@Entity
@Table(name = "risk_profiles")
public class RiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One student → one risk profile (one-to-one semantically, but modeled here for flexibility)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private User student;

    // Score 0–100
    @Column(name = "current_score", nullable = false)
    private Double currentScore = 0.0;

    @Column(name = "total_timeouts")
    private Integer totalTimeouts = 0;

    @Column(name = "total_overstays")
    private Integer totalOverstays = 0;

    @Column(name = "total_off_hour_exits")
    private Integer totalOffHourExits = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────
    public RiskProfile() {}

    public RiskProfile(User student) {
        this.student = student;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId() { return id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Double getCurrentScore() { return currentScore; }
    public void setCurrentScore(Double currentScore) { this.currentScore = currentScore; }

    public Integer getTotalTimeouts() { return totalTimeouts; }
    public void setTotalTimeouts(Integer totalTimeouts) { this.totalTimeouts = totalTimeouts; }

    public Integer getTotalOverstays() { return totalOverstays; }
    public void setTotalOverstays(Integer totalOverstays) { this.totalOverstays = totalOverstays; }

    public Integer getTotalOffHourExits() { return totalOffHourExits; }
    public void setTotalOffHourExits(Integer totalOffHourExits) { this.totalOffHourExits = totalOffHourExits; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}