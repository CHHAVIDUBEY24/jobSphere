package com.chhavi.firstjobapp.auth;

public class AuthResponse {
    private String token;
    private String message;
    private String role;

    public AuthResponse(String token, String message, String role) {
        this.token = token;
        this.message = message;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getMessage() { return message; }
    public String getRole() { return role; }
}