/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ChatService.java
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Service for managing chat messages and conversations.
 *              - Handles sending messages, retrieving history, and marking messages as read.
 *              - Manages conversation grouping by context (book or order).
 * Others:
 * Function List:
 * 1. send - Sends a chat message.
 * 2. markRead - Marks messages from a specific user as read.
 * 3. countTotalUnread - Counts total unread messages for a user.
 * 4. history - Retrieves chat history for a specific conversation context.
 * 5. listConversations - Lists all conversations for a user with last message and unread count.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

package com.whu.bookapi.service;

import com.whu.bookapi.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Chat/Message management.
 */
@Service
public class ChatService {
    private final JdbcTemplate jdbcTemplate;

    public ChatService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: key
     * Description: Generates a unique conversation key based on participants and context.
     *              Format: [B{bookId}|O{orderId}]:user1:user2 (users sorted alphabetically).
     * Calls: None
     * Called By: send, history
     * Input: a (String), b (String), bookId (Long), orderId (Long)
     * Output: String (conversation key)
     * Return: String
     */
    private String key(String a, String b, Long bookId, Long orderId) {
        String u1 = a.compareTo(b) <= 0 ? a : b;
        String u2 = a.compareTo(b) <= 0 ? b : a;
        return (bookId != null ? "B" + bookId : "O" + (orderId == null ? 0 : orderId)) + ":" + u1 + ":" + u2;
    }

    /**
     * Function: send
     * Description: Sends a chat message.
     *              Generates conversation key, inserts message into database, and sets ID.
     * Calls: key, JdbcTemplate.update
     * Called By: ChatController.send
     * Table Accessed: chat_message
     * Table Updated: chat_message
     * Input: m (ChatMessage)
     * Output: ChatMessage (with ID set)
     * Return: ChatMessage
     */
    public ChatMessage send(ChatMessage m) {
        if (m == null || m.getFromUser() == null || m.getToUser() == null) return null;
        m.setCreateTime(System.currentTimeMillis());
        m.setRead(false);
        String k = key(m.getFromUser(), m.getToUser(), m.getBookId(), m.getOrderId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO chat_message (conv_key, from_user, to_user, book_id, order_id, content, create_time, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, k);
            ps.setString(2, m.getFromUser());
            ps.setString(3, m.getToUser());
            if (m.getBookId() == null) ps.setObject(4, null);
            else ps.setLong(4, m.getBookId());
            if (m.getOrderId() == null) ps.setObject(5, null);
            else ps.setLong(5, m.getOrderId());
            ps.setString(6, m.getContent());
            ps.setLong(7, m.getCreateTime());
            return ps;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id != null) m.setId(id.longValue());
        return m;
    }

    /**
     * Function: markRead
     * Description: Marks all unread messages from a specific peer as read for the current user.
     * Calls: JdbcTemplate.update
     * Called By: ChatController.markRead
     * Table Accessed: chat_message
     * Table Updated: chat_message
     * Input: username (String - current user), peer (String - sender)
     * Output: None
     * Return: void
     */
    public void markRead(String username, String peer) {
        if (username == null || peer == null) return;
        jdbcTemplate.update(
                "UPDATE chat_message SET is_read = 1 WHERE to_user = ? AND from_user = ? AND is_read = 0",
                username,
                peer
        );
    }

    /**
     * Function: countTotalUnread
     * Description: Counts total unread messages for the user across all conversations.
     * Calls: JdbcTemplate.queryForObject
     * Called By: ChatController.countUnread
     * Table Accessed: chat_message
     * Table Updated: None
     * Input: username (String)
     * Output: long
     * Return: long
     */
    public long countTotalUnread(String username) {
        if (username == null) return 0;
        Long c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM chat_message WHERE to_user = ? AND is_read = 0",
                Long.class,
                username
        );
        return c == null ? 0 : c;
    }

    /**
     * Function: history
     * Description: Retrieves chat history between two users for a specific context (book or order).
     * Calls: key, JdbcTemplate.query
     * Called By: ChatController.history
     * Table Accessed: chat_message
     * Table Updated: None
     * Input: a (String), b (String), bookId (Long), orderId (Long)
     * Output: List<ChatMessage>
     * Return: List<ChatMessage>
     */
    public List<ChatMessage> history(String a, String b, Long bookId, Long orderId) {
        String k = key(a, b, bookId, orderId);
        return jdbcTemplate.query(
                "SELECT id, from_user, to_user, book_id, order_id, content, create_time, is_read FROM chat_message WHERE conv_key = ? ORDER BY create_time ASC, id ASC",
                (rs, rowNum) -> {
                    ChatMessage m = new ChatMessage();
                    m.setId(rs.getLong("id"));
                    m.setFromUser(rs.getString("from_user"));
                    m.setToUser(rs.getString("to_user"));
                    Object bid = rs.getObject("book_id");
                    if (bid != null) m.setBookId(((Number) bid).longValue());
                    Object oid = rs.getObject("order_id");
                    if (oid != null) m.setOrderId(((Number) oid).longValue());
                    m.setContent(rs.getString("content"));
                    m.setCreateTime(rs.getLong("create_time"));
                    m.setRead(rs.getInt("is_read") != 0);
                    return m;
                },
                k
        );
    }

    /**
     * Function: listConversations
     * Description: Lists all conversations for the user, aggregated by conversation key.
     *              Includes the last message content, time, and unread count for each conversation.
     * Calls: JdbcTemplate.queryForList
     * Called By: ChatController.listConversations
     * Table Accessed: chat_message
     * Table Updated: None
     * Input: username (String)
     * Output: List<Map<String, Object>>
     * Return: List<Map<String, Object>>
     */
    public java.util.List<java.util.Map<String, Object>> listConversations(String username) {
        if (username == null) return new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT agg.conv_key, agg.last_id, agg.last_time, agg.unread, m.content AS last_content " +
                        "FROM (" +
                        "  SELECT conv_key, MAX(id) AS last_id, MAX(create_time) AS last_time, " +
                        "         SUM(CASE WHEN to_user = ? AND is_read = 0 THEN 1 ELSE 0 END) AS unread " +
                        "  FROM chat_message " +
                        "  WHERE from_user = ? OR to_user = ? " +
                        "  GROUP BY conv_key" +
                        ") agg " +
                        "JOIN chat_message m ON m.id = agg.last_id " +
                        "ORDER BY agg.last_time DESC",
                username,
                username,
                username
        );

        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String k = (String) row.get("conv_key");
            String[] parts = k == null ? null : k.split(":", 3);
            if (parts == null || parts.length < 3) continue;
            String ctx = parts[0];
            String u1 = parts[1];
            String u2 = parts[2];
            if (!username.equals(u1) && !username.equals(u2)) continue;
            String peer = username.equals(u1) ? u2 : u1;
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("peer", peer);
            if (ctx.startsWith("B")) item.put("bookId", Long.parseLong(ctx.substring(1)));
            else if (ctx.startsWith("O")) item.put("orderId", Long.parseLong(ctx.substring(1)));
            item.put("lastContent", row.get("last_content"));
            item.put("lastTime", ((Number) row.getOrDefault("last_time", 0L)).longValue());
            item.put("unread", ((Number) row.getOrDefault("unread", 0)).intValue());
            res.add(item);
        }
        return res;
    }
}
