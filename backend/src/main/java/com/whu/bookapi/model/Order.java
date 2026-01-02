package com.whu.bookapi.model;

import java.time.LocalDateTime;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Order.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Model class representing a transaction order.
 *              Includes buyer, seller, book details, and order status.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class Order {
    /** Unique ID of the order */
    private Long id;
    /** ID of the book being purchased */
    private Long bookId;
    /** Title of the book */
    private String bookName;
    /** Username of the seller */
    private String sellerName;
    /** Price at the time of order */
    private Double price;
    /** Username of the buyer */
    private String buyerName;
    /** Order status (pending, paid, received, cancelled) */
    private String status; 
    /** Timestamp of order creation */
    private LocalDateTime createTime;
    /** Timestamp when the order expires (if not paid) */
    private Long expireAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Long getExpireAt() { return expireAt; }
    public void setExpireAt(Long expireAt) { this.expireAt = expireAt; }
}
