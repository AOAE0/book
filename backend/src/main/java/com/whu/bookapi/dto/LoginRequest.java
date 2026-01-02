/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: LoginRequest.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: DTO for user login requests.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for Login Request.
 */
public class LoginRequest {
    /** Username or ID */
    @NotBlank
    private String username;
    
    /** User password */
    @NotBlank
    private String password;
    
    /** User role (optional/validated on server) */
    @NotBlank
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
