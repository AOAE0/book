package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: OrderService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Service class for managing orders.
 *              Handles order creation, retrieval, status updates, and lifecycle management.
 *              Includes logic for order expiration (15 minutes).
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2024-11-20      1.0                Initial implementation.
 * WiseBookPal Team  2024-12-25      1.1                Added expiration logic.
 */
@Service
public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: create
     * Description: Creates a new order for a specific book.
     *              Sets initial status to 'pending' and calculates expiration time (15 minutes from creation).
     * Called By: OrderController.createOrder
     * Table Accessed: orders
     * Table Updated: orders
     * Input: book (Book) - The book being purchased
     *        buyerName (String) - The username of the buyer
     * Output: Order - The created order object with generated ID
     * Return: Order
     */
    public Order create(Book book, String buyerName) {
        if (book == null || buyerName == null) return null;
        Order o = new Order();
        o.setBookId(book.getId());
        o.setBookName(book.getBookName());
        o.setSellerName(book.getSellerName());
        o.setPrice(book.getSellPrice());
        o.setBuyerName(buyerName);
        o.setStatus("pending");
        o.setCreateTime(LocalDateTime.now());
        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO orders (book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, book.getId());
            ps.setString(2, book.getBookName());
            ps.setString(3, book.getSellerName());
            ps.setObject(4, book.getSellPrice());
            ps.setString(5, buyerName);
            ps.setString(6, o.getStatus());
            ps.setLong(7, o.getExpireAt() == null ? 0 : o.getExpireAt());
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(o.getCreateTime()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) o.setId(key.longValue());
        return o;
    }

    /**
     * Function: listByBuyer
     * Description: Retrieves all orders placed by a specific buyer.
     *              Calculates expiration time on the fly if missing.
     * Called By: OrderController.listByBuyer, UserService.checkUncompletedOrders (indirectly via logic checks)
     * Table Accessed: orders
     * Input: buyerName (String) - The username of the buyer
     * Output: List<Order> - List of orders for the buyer
     * Return: List<Order>
     */
    public List<Order> listByBuyer(String buyerName) {
        if (buyerName == null) return java.util.Collections.emptyList();
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE buyer_name = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                },
                buyerName
        );
    }

    /**
     * Function: listBySeller
     * Description: Retrieves all orders for books sold by a specific seller.
     *              Calculates expiration time on the fly if missing.
     * Called By: OrderController.listBySeller, UserService.checkUncompletedOrders (indirectly via logic checks)
     * Table Accessed: orders
     * Input: sellerName (String) - The username of the seller
     * Output: List<Order> - List of orders for the seller
     * Return: List<Order>
     */
    public List<Order> listBySeller(String sellerName) {
        if (sellerName == null) return java.util.Collections.emptyList();
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE seller_name = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                },
                sellerName
        );
    }

    /**
     * Function: get
     * Description: Retrieves an order by its ID.
     * Called By: OrderController.getOrder
     * Table Accessed: orders
     * Input: id (Long) - The ID of the order to retrieve
     * Output: Order - The order object, or null if not found
     * Return: Order
     */
    public Order get(Long id) {
        if (id == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE id = ?",
                    (rs, rowNum) -> {
                        Order o = new Order();
                        o.setId(rs.getLong("id"));
                        o.setBookId(rs.getLong("book_id"));
                        o.setBookName(rs.getString("book_name"));
                        o.setSellerName(rs.getString("seller_name"));
                        o.setPrice((Double) rs.getObject("price"));
                        o.setBuyerName(rs.getString("buyer_name"));
                        o.setStatus(rs.getString("status"));
                        long expireAt = rs.getLong("expire_at");
                        if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                        java.sql.Timestamp ts = rs.getTimestamp("create_time");
                        o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                        if (o.getExpireAt() == null && o.getCreateTime() != null) {
                            o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                        }
                        return o;
                    },
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Function: setStatus
     * Description: Updates the status of an order.
     * Called By: OrderController.payOrder, OrderController.cancelOrder, OrderController.completeOrder
     * Table Accessed: orders
     * Table Updated: orders
     * Input: id (Long) - The ID of the order to update
     *        status (String) - The new status (e.g., 'paid', 'cancelled', 'completed')
     * Output: Order - The updated order object
     * Return: Order
     */
    public Order setStatus(Long id, String status) {
        if (id == null || status == null) return null;
        jdbcTemplate.update("UPDATE orders SET status = ? WHERE id = ?", status, id);
        return get(id);
    }

    /**
     * Function: listAll
     * Description: Retrieves all orders in the system.
     *              Used for administrative purposes.
     * Called By: AdminController.listOrders
     * Table Accessed: orders
     * Input: None
     * Output: List<Order> - List of all orders
     * Return: List<Order>
     */
    public List<Order> listAll() {
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                }
        );
    }
}
