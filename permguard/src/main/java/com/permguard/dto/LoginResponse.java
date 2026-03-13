package com.permguard.dto;

public class LoginResponse {

    private String  token;
    private String  role;
    private Long    userId;
    private String  fullName;
    private String  email;
    private String  rollNumber;
    private Boolean mustChangePassword;
    private String  message;

    public LoginResponse() {}

    // ── Getters & Setters ─────────────────────────────────────
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // ── Builder ───────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final LoginResponse obj = new LoginResponse();

        public Builder token(String token)                         { obj.token = token; return this; }
        public Builder role(String role)                           { obj.role = role; return this; }
        public Builder userId(Long userId)                         { obj.userId = userId; return this; }
        public Builder fullName(String fullName)                   { obj.fullName = fullName; return this; }
        public Builder email(String email)                         { obj.email = email; return this; }
        public Builder rollNumber(String rollNumber)               { obj.rollNumber = rollNumber; return this; }
        public Builder mustChangePassword(Boolean v)               { obj.mustChangePassword = v; return this; }
        public Builder message(String message)                     { obj.message = message; return this; }
        public LoginResponse build()                               { return obj; }
    }
}