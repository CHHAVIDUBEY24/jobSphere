package com.chhavi.firstjobapp.auth;

public class AuthResponse {
    private String token;
    private String message;
    private String role;
    private Long companyId;
    private String companyName;

    public AuthResponse(String token, String message, String role, Long companyId, String companyName) {
        this.token = token;
        this.message = message;
        this.role = role;
        this.companyId = companyId;
        this.companyName = companyName;
    }

    public String getToken() { return token; }
    public String getMessage() { return message; }
    public String getRole() { return role; }
    public Long getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
}