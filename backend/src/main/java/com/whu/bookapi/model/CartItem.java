package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: CartItem.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing an item in the shopping cart.
 *              Links a user to a specific book with quantity.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class CartItem {
    /** ID of the book in the cart */
    private Long bookId;
    /** Username of the cart owner */
    private String username;
    /** Quantity of the book (default 1) */
    private Integer quantity = 1;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
