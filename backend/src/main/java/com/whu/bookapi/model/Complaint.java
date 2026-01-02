package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Complaint.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing a user complaint.
 *              Includes complaint details, type, and resolution status.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class Complaint {
    /** Unique ID of the complaint */
    private Long id;
    /** ID of the order related to the complaint (optional) */
    private Long orderId;
    /** Username of the complainant */
    private String username;
    /** Type of complaint (e.g., fraud, harassment) */
    private String type;
    /** Detailed description of the complaint */
    private String detail;
    /** Timestamp of complaint creation */
    private long createTime;
    /** Resolution status (pending, handling, resolved, rejected) */
    private String status; 
    /** Reason for the resolution decision */
    private String auditReason;
    /** Timestamp of resolution */
    private Long auditTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Long getAuditTime() { return auditTime; }
    public void setAuditTime(Long auditTime) { this.auditTime = auditTime; }
}
