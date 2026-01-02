package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: User.java
 * Author: WiseBookPal Team Version: 1.1 Date: 2026-01-02
 * Description: Model class representing a user in the system.
 *              Includes personal info, credentials, roles, and status.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added status, creditScore, studentId, lastLoginTime for U14
 */
public class User {
    /** User's unique username */
    private String username;
    /** User's role (admin, buyer, seller) */
    private String role;
    /** Session token */
    private String token;
    /** Seller application status (NONE, PENDING, APPROVED, REJECTED) */
    private String sellerStatus; 
    /** ID of the payment code file */
    private Long paymentCodeFileId;
    /** URL of the payment code image */
    private String paymentCodeUrl;
    /** User's phone number */
    private String phone;
    /** User's email address */
    private String email;
    /** User's gender (male, female, secret) */
    private String gender;
    /** Account status (normal, blacklist, deleted) */
    private String status; 
    /** Credit score (default 100) */
    private int creditScore;
    /** Student ID number */
    private String studentId;
    /** Timestamp of last login */
    private Long lastLoginTime;
    /** Timestamp of registration */
    private Long createdAt; 

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(String sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public Long getPaymentCodeFileId() {
        return paymentCodeFileId;
    }

    public void setPaymentCodeFileId(Long paymentCodeFileId) {
        this.paymentCodeFileId = paymentCodeFileId;
    }

    public String getPaymentCodeUrl() {
        return paymentCodeUrl;
    }

    public void setPaymentCodeUrl(String paymentCodeUrl) {
        this.paymentCodeUrl = paymentCodeUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
