package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ChatMessage.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing a chat message between users.
 *              Supports context linking to books or orders.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class ChatMessage {
    /** Unique ID of the message */
    private Long id;
    /** Username of the sender */
    private String fromUser;
    /** Username of the recipient */
    private String toUser;
    /** ID of the related book (optional context) */
    private Long bookId;
    /** ID of the related order (optional context) */
    private Long orderId;
    /** Text content of the message */
    private String content;
    /** Timestamp of message creation */
    private long createTime;
    /** Read status of the message */
    private boolean read;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }
    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
