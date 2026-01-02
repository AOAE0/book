package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Book.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Model class representing a second-hand book.
 *              Includes book details, seller info, pricing, and condition.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class Book {
    /** Unique ID of the book listing */
    private Long id;
    /** Title of the book */
    private String bookName;
    /** Author of the book */
    private String author;
    /** Original market price */
    private Double originalPrice;
    /** Selling price */
    private Double sellPrice;
    /** Description of the book */
    private String description;
    /** Username of the seller */
    private String sellerName;
    /** URL of the book cover image */
    private String coverUrl;
    /** ISBN number */
    private String isbn;
    /** Publisher name */
    private String publisher;
    /** Publication date */
    private String publishDate;
    /** Condition level (e.g., New, Good, Fair) */
    private String conditionLevel;
    /** Available stock quantity */
    private Integer stock;
    /** Listing status (active, sold, etc.) */
    private String status;
    /** Timestamp of listing creation */
    private Long createdAt;
    /** Seller type (student, teacher) */
    private String sellerType; 
    /** Reason for audit/moderation decision */
    private String auditReason;
    /** Timestamp of audit */
    private Long auditTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(String conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getSellerType() {
        return sellerType;
    }

    public void setSellerType(String sellerType) {
        this.sellerType = sellerType;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public Long getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Long auditTime) {
        this.auditTime = auditTime;
    }
}
