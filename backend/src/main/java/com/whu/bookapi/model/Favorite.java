package com.whu.bookapi.model;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Favorite.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Model class representing a user's favorite book.
 *              Used for managing the wishlist/favorites list.
 * Others:
 * Function List:
 * 1. Getters and Setters for all fields.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
public class Favorite {
    /** ID of the favorited book */
    private Long bookId;
    /** Username of the user who favorited the book */
    private String username;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
