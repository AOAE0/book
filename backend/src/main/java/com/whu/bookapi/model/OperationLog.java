package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: OperationLog.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing an administrative operation log.
 *              Used for audit trails of sensitive actions.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation for U14
 */
public class OperationLog {
    /** Unique ID of the log entry */
    private Long id;
    /** Username of the admin who performed the action */
    private String operator;
    /** Username of the user affected by the action */
    private String targetUser;
    /** Type of action (e.g., blacklist, unblacklist, update_info) */
    private String action; 
    /** Detailed description or reason for the action */
    private String detail;
    /** Timestamp of the operation */
    private long createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}
