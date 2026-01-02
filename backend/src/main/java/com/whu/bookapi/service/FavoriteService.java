/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: FavoriteService.java
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Service for managing user favorites (book collection).
 *              - Handles adding/removing books from favorites.
 *              - Checks collection status.
 *              - Lists favorite books or their IDs.
 * Others:
 * Function List:
 * 1. add - Adds a book to favorites.
 * 2. remove - Removes a book from favorites.
 * 3. isCollected - Checks if a book is in favorites.
 * 4. listIds - Lists IDs of all favorite books.
 * 5. list - Lists full details of favorite books.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class for Favorites/Collection management.
 */
@Service
public class FavoriteService {
    private final JdbcTemplate jdbcTemplate;

    public FavoriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: add
     * Description: Adds a book to the user's favorites list.
     *              Uses INSERT IGNORE to prevent duplicates.
     * Calls: JdbcTemplate.update
     * Called By: FavoriteController.add
     * Table Accessed: favorites
     * Table Updated: favorites
     * Input: username (String), bookId (Long)
     * Output: None
     * Return: void
     */
    public void add(String username, Long bookId) {
        if (username == null || bookId == null) return;
        jdbcTemplate.update(
                "INSERT IGNORE INTO favorites (username, book_id, created_at) VALUES (?, ?, ?)",
                username,
                bookId,
                System.currentTimeMillis()
        );
    }

    /**
     * Function: remove
     * Description: Removes a book from the user's favorites list.
     * Calls: JdbcTemplate.update
     * Called By: FavoriteController.remove
     * Table Accessed: favorites
     * Table Updated: favorites
     * Input: username (String), bookId (Long)
     * Output: None
     * Return: void
     */
    public void remove(String username, Long bookId) {
        if (username == null || bookId == null) return;
        jdbcTemplate.update("DELETE FROM favorites WHERE username = ? AND book_id = ?", username, bookId);
    }

    /**
     * Function: isCollected
     * Description: Checks if a specific book is in the user's favorites.
     * Calls: JdbcTemplate.queryForObject
     * Called By: FavoriteController.checkStatus
     * Table Accessed: favorites
     * Table Updated: None
     * Input: username (String), bookId (Long)
     * Output: boolean
     * Return: boolean
     */
    public boolean isCollected(String username, Long bookId) {
        if (username == null || bookId == null) return false;
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM favorites WHERE username = ? AND book_id = ?",
                Integer.class,
                username,
                bookId
        );
        return c != null && c > 0;
    }

    /**
     * Function: listIds
     * Description: Retrieves the list of book IDs favorited by the user.
     * Calls: JdbcTemplate.queryForList
     * Called By: FavoriteController.listIds
     * Table Accessed: favorites
     * Table Updated: None
     * Input: username (String)
     * Output: List<Long>
     * Return: List<Long>
     */
    public List<Long> listIds(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.queryForList(
                "SELECT book_id FROM favorites WHERE username = ? ORDER BY created_at DESC",
                Long.class,
                username
        );
    }

    /**
     * Function: list
     * Description: Retrieves full details of books favorited by the user.
     *              Only returns books that are currently 'on_sale' and have stock > 0.
     * Calls: JdbcTemplate.query
     * Called By: FavoriteController.list
     * Table Accessed: favorites, books
     * Table Updated: None
     * Input: username (String)
     * Output: List<Book>
     * Return: List<Book>
     */
    public List<Book> list(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT b.id, b.book_name, b.author, b.original_price, b.sell_price, b.description, b.seller_name, b.cover_url, b.isbn, b.publisher, b.publish_date, b.condition_level, b.stock, b.status, b.created_at, b.seller_type " +
                        "FROM favorites f JOIN books b ON f.book_id = b.id " +
                        "WHERE f.username = ? AND b.status = 'on_sale' AND b.stock > 0 " +
                        "ORDER BY f.created_at DESC",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                username
        );
    }
}
