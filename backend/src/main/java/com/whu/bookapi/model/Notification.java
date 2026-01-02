package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Notification.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing a system notification.
 *              Supports targeted (single user) and broadcast (all users) notifications.
 * Others:
 * Function List:
 * 1. isReadBy - Checks if the notification has been read by a user.
 * 2. markReadBy - Marks the notification as read for a user.
 * 3. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class Notification {
    /** Unique ID of the notification */
    private Long id;
    /** Recipient username or "*" for broadcast */
    private String toUser; 
    /** Type of notification (announcement, audit, complaint, system) */
    private String type;   
    /** Title of the notification */
    private String title;
    /** Content/Body of the notification */
    private String content;
    /** Timestamp of creation */
    private long createTime;
    /** Read status (for single-user notifications) */
    private boolean read;
    /** Set of usernames who have read this broadcast notification */
    private java.util.Set<String> readByUsers = new java.util.HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public java.util.Set<String> getReadByUsers() { return readByUsers; }
    public void setReadByUsers(java.util.Set<String> readByUsers) { this.readByUsers = readByUsers; }
    
    /**
     * Function: isReadBy
     * Description: Helper to check if read by a specific user.
     * Input: username (String) - User to check
     * Output: boolean - True if read
     */
    public boolean isReadBy(String username) {
        if ("*".equals(toUser)) {
            return readByUsers.contains(username);
        }
        return read;
    }

    /**
     * Function: markReadBy
     * Description: Marks the notification as read for a specific user.
     * Input: username (String) - User to mark as read
     */
    public void markReadBy(String username) {
        if ("*".equals(toUser)) {
            readByUsers.add(username);
        } else {
            this.read = true;
        }
    }
}
