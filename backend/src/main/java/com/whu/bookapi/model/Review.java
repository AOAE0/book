package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Review.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing a review for an order.
 *              Includes scores, comments, tags, and moderation status.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation with moderation fields
 */
public class Review {
    /** Unique ID of the review */
    private Long id;
    /** ID of the associated order */
    private Long orderId;
    /** Username of the reviewer */
    private String username;
    /** Score for book condition (1-5) */
    private int scoreCondition;
    /** Score for service (1-5) */
    private int scoreService;
    /** Text comment */
    private String comment;
    /** Timestamp of review creation */
    private long createTime;
    /** List of tags describing the review */
    private java.util.List<String> tags;
    /** Moderation status (pending, approved, rejected) */
    private String status; 
    /** Reason for moderation decision */
    private String auditReason;
    /** Timestamp of moderation */
    private Long auditTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getScoreCondition() { return scoreCondition; }
    public void setScoreCondition(int scoreCondition) { this.scoreCondition = scoreCondition; }
    public int getScoreService() { return scoreService; }
    public void setScoreService(int scoreService) { this.scoreService = scoreService; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Long getAuditTime() { return auditTime; }
    public void setAuditTime(Long auditTime) { this.auditTime = auditTime; }
}
