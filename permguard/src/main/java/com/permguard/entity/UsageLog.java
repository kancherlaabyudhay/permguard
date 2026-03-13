package com.permguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_logs")
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = true)
    private Permission permission;

    @Column(name = "scanned_by")
    private Long scannedBy;

    @Column(name = "scan_type", length = 20)
    private String scanType;

    @Column(name = "outcome", length = 20)
    private String outcome;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(name = "timeout_flag")
    private Boolean timeoutFlag;

    // ---- Constructors ----

    public UsageLog() {}

    // ---- Getters & Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }

    public Long getScannedBy() { return scannedBy; }
    public void setScannedBy(Long scannedBy) { this.scannedBy = scannedBy; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean getTimeoutFlag() { return timeoutFlag; }
    public void setTimeoutFlag(Boolean timeoutFlag) { this.timeoutFlag = timeoutFlag; }
}