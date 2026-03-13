
package com.permguard.dto;

import com.permguard.entity.User;

// ================================================================
//  UserResponse DTO
//  Returned when listing or creating users — never exposes password
// ================================================================

public class UserResponse {

    private Long    userId;
    private String  rollNumber;
    private String  fullName;
    private String  email;
    private String  phone;
    private String  role;
    private String  deptName;
    private Integer yearOfStudy;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private String  createdAt;

    // Static factory — converts User entity to UserResponse
    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.userId             = user.getUserId();
        r.rollNumber         = user.getRollNumber();
        r.fullName           = user.getFullName();
        r.email              = user.getEmail();
        r.phone              = user.getPhone();
        r.role               = user.getRole().name();
        r.deptName           = user.getDepartment() != null
                                ? user.getDepartment().getDeptName() : null;
        r.yearOfStudy        = user.getYearOfStudy();
        r.isActive           = user.getIsActive();
        r.mustChangePassword = user.getMustChangePassword();
        r.createdAt          = user.getCreatedAt() != null
                                ? user.getCreatedAt().toString() : null;
        return r;
    }

    // Getters
    public Long    getUserId()             { return userId; }
    public String  getRollNumber()         { return rollNumber; }
    public String  getFullName()           { return fullName; }
    public String  getEmail()              { return email; }
    public String  getPhone()              { return phone; }
    public String  getRole()               { return role; }
    public String  getDeptName()           { return deptName; }
    public Integer getYearOfStudy()        { return yearOfStudy; }
    public Boolean getIsActive()           { return isActive; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public String  getCreatedAt()          { return createdAt; }
}
