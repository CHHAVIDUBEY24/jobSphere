package com.chhavi.firstjobapp.auth;

import java.time.LocalDate;

public class AuthRequest {
    private String username;
    private String password;
    private LocalDate dateOfBirth;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}