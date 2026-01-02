/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: CartService.java
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Service for managing shopping cart operations.
 *              - Handles adding, removing, clearing, and listing cart items.
 *              - Validates stock and prevents self-purchasing.
 * Others:
 * Function List:
 * 1. add - Adds a book to the cart.
 * 2. remove - Removes a book from the cart (optionally specific quantity).
 * 3. clear - Clears all items from the user's cart.
 * 4. list - Lists all items in the user's cart.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Shopping Cart management.
 */
@Service
public class CartService {
    private final BookService bookService;
    private final JdbcTemplate jdbcTemplate;

    public CartService(BookService bookService, JdbcTemplate jdbcTemplate) {
        this.bookService = bookService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: add
     * Description: Adds a book to the user's shopping cart.
     *              Validates if the book exists, if the user is the seller (cannot buy own book),
     *              and if there is sufficient stock.
     * Calls: BookService.get, JdbcTemplate.queryForObject, JdbcTemplate.update
     * Called By: CartController.add
     * Table Accessed: cart_item, books (via BookService)
     * Table Updated: cart_item
     * Input: username (String), bookId (Long)
     * Output: None
     * Return: void
     * Others: Throws RuntimeException if validation fails.
     */
    public void add(String username, Long bookId) {
        Book book = bookService.get(bookId);
        if (book == null) {
            throw new RuntimeException("Book not found");
        }
        
        if (username.equals(book.getSellerName())) {
            throw new RuntimeException("Cannot buy your own book");
        }

        Integer existingQty = null;
        try {
            existingQty = jdbcTemplate.queryForObject(
                    "SELECT quantity FROM cart_item WHERE username = ? AND book_id = ?",
                    Integer.class,
                    username,
                    bookId
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
        }
        int currentQty = existingQty == null ? 0 : existingQty;
        int stock = book.getStock() == null ? 0 : book.getStock();
        if (currentQty + 1 > stock) {
            throw new RuntimeException("Inventory insufficient");
        }
        jdbcTemplate.update(
                "INSERT INTO cart_item (username, book_id, quantity) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1",
                username,
                bookId
        );
    }

    /**
     * Function: remove
     * Description: Removes a book from the cart (entirely).
     * Calls: remove(username, bookId, null)
     * Called By: CartController.remove
     * Input: username (String), bookId (Long)
     * Output: None
     * Return: void
     */
    public void remove(String username, Long bookId) {
        remove(username, bookId, null);
    }

    /**
     * Function: remove
     * Description: Removes a specific quantity of a book from the cart.
     *              If count is null or <= 0, removes the item entirely.
     *              If count >= existing quantity, removes the item entirely.
     * Calls: JdbcTemplate.queryForObject, JdbcTemplate.update
     * Called By: remove(username, bookId)
     * Table Accessed: cart_item
     * Table Updated: cart_item
     * Input: username (String), bookId (Long), count (Integer)
     * Output: None
     * Return: void
     */
    public void remove(String username, Long bookId, Integer count) {
        if (username == null || bookId == null) return;
        if (count == null || count <= 0) {
            jdbcTemplate.update("DELETE FROM cart_item WHERE username = ? AND book_id = ?", username, bookId);
            return;
        }
        Integer existingQty = null;
        try {
            existingQty = jdbcTemplate.queryForObject(
                    "SELECT quantity FROM cart_item WHERE username = ? AND book_id = ?",
                    Integer.class,
                    username,
                    bookId
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
        }
        if (existingQty == null) return;
        if (count >= existingQty) {
            jdbcTemplate.update("DELETE FROM cart_item WHERE username = ? AND book_id = ?", username, bookId);
        } else {
            jdbcTemplate.update(
                    "UPDATE cart_item SET quantity = quantity - ? WHERE username = ? AND book_id = ?",
                    count,
                    username,
                    bookId
            );
        }
    }

    /**
     * Function: clear
     * Description: Removes all items from the user's cart.
     * Calls: JdbcTemplate.update
     * Called By: CartController.clear
     * Table Accessed: None
     * Table Updated: cart_item
     * Input: username (String)
     * Output: None
     * Return: void
     */
    public void clear(String username) {
        jdbcTemplate.update("DELETE FROM cart_item WHERE username = ?", username);
    }

    /**
     * Function: list
     * Description: Retrieves all items in the user's cart.
     * Calls: JdbcTemplate.queryForList
     * Called By: CartController.list
     * Table Accessed: cart_item
     * Table Updated: None
     * Input: username (String)
     * Output: List<CartItem>
     * Return: List<CartItem>
     */
    public List<CartItem> list(String username) {
        List<CartItem> res = new ArrayList<>();
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT book_id, quantity FROM cart_item WHERE username = ? ORDER BY book_id",
                username
        );
        for (java.util.Map<String, Object> row : rows) {
            CartItem ci = new CartItem();
            ci.setUsername(username);
            ci.setBookId(((Number) row.get("book_id")).longValue());
            ci.setQuantity(((Number) row.get("quantity")).intValue());
            res.add(ci);
        }
        return res;
    }
}
