/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: LoginResponse.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: DTO for login response containing token and user info.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi.dto;

/**
 * Data Transfer Object for Login Response.
 */
public class LoginResponse {
    /** JWT Token */
    private String token;
    
    /** Username */
    private String username;
    
    /** User Role */
    private String role;
    
    /** Seller Status (e.g., NONE, PENDING, APPROVED) */
    private String sellerStatus;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(String sellerStatus) {
        this.sellerStatus = sellerStatus;
    }
}
