package com.permguard.dto;

import com.permguard.entity.Permission;

// ================================================================
//  PermissionResponse DTO — returned to client
// ================================================================

public class PermissionResponse {

    private Long   id;
    private String studentName;
    private String studentRoll;
    private String facultyName;
    private String type;
    private String reason;
    private String status;
    private String expiryTime;
    private String approvedAt;
    private String createdAt;
    private String qrToken;

    public static PermissionResponse from(Permission p) {
        PermissionResponse r = new PermissionResponse();
        r.id          = p.getId();
        r.type        = p.getType();
        r.reason      = p.getReason();
        r.status      = p.getStatus().name();
        r.qrToken     = p.getQrToken();
        r.expiryTime  = p.getExpiryTime() != null ? p.getExpiryTime().toString() : null;
        r.approvedAt  = p.getApprovedAt() != null ? p.getApprovedAt().toString() : null;
        r.createdAt   = p.getCreatedAt() != null  ? p.getCreatedAt().toString()  : null;

        if (p.getStudent() != null) {
            r.studentName = p.getStudent().getFullName();
            r.studentRoll = p.getStudent().getRollNumber();
        }
        if (p.getFaculty() != null) {
            r.facultyName = p.getFaculty().getFullName();
        }
        return r;
    }

    // Getters
    public Long   getId()          { return id; }
    public String getStudentName() { return studentName; }
    public String getStudentRoll() { return studentRoll; }
    public String getFacultyName() { return facultyName; }
    public String getType()        { return type; }
    public String getReason()      { return reason; }
    public String getStatus()      { return status; }
    public String getExpiryTime()  { return expiryTime; }
    public String getApprovedAt()  { return approvedAt; }
    public String getCreatedAt()   { return createdAt; }
    public String getQrToken()     { return qrToken; }
}