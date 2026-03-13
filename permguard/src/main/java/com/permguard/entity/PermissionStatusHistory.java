package com.permguard.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// ================================================================
//  PermissionStatusHistory — audit trail for every status change
//  Maps to `permission_status_history` table
// ================================================================

@Entity
@Table(name = "permission_status_history")
public class PermissionStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50, nullable = false)
    private String newStatus;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    // ── Constructors ──────────────────────────────────────────
    public PermissionStatusHistory() {}

    public PermissionStatusHistory(Permission permission, String oldStatus, String newStatus) {
        this.permission = permission;
        this.oldStatus  = oldStatus;
        this.newStatus  = newStatus;
    }

    // ── Getters ───────────────────────────────────────────────
    public Long getId() { return id; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
}