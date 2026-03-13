package com.permguard.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// ================================================================
//  Alert — generated when a student's risk score crosses a threshold
//  Maps to `alerts` table
// ================================================================

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "resolved")
    private Boolean resolved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }

    // ── Constructors ──────────────────────────────────────────
    public Alert() {}

    public Alert(User student, RiskLevel riskLevel, String message) {
        this.student   = student;
        this.riskLevel = riskLevel;
        this.message   = message;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId() { return id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}