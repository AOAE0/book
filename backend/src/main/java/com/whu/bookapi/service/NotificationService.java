package com.whu.bookapi.service;

import com.whu.bookapi.model.Notification;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: NotificationService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Service class for managing system and user notifications.
 *              Handles targeted messages, broadcast messages, and read status tracking.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2024-11-20      1.0                Initial implementation.
 */
@Service
public class NotificationService {
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: addToUser
     * Description: Sends a notification to a specific user.
     * Called By: UserService.updateUserStatus, UserService.deleteUser
     * Table Accessed: notifications
     * Table Updated: notifications
     * Input: username (String) - Recipient's username
     *        type (String) - Notification type
     *        title (String) - Notification title
     *        content (String) - Notification body
     * Output: Notification - The created notification
     * Return: Notification
     */
    public Notification addToUser(String username, String type, String title, String content) {
        Notification n = new Notification();
        n.setToUser(username);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        n.setRead(false);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO notifications (to_user, type, title, content, create_time, is_read) VALUES (?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, type);
            ps.setString(3, title);
            ps.setString(4, content);
            ps.setLong(5, n.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) n.setId(key.longValue());
        return n;
    }

    /**
     * Function: add
     * Description: Simplified method to send a system notification to a user.
     * Called By: Various service methods for simple alerts.
     * Input: username (String) - Recipient's username
     *        content (String) - Notification body
     * Output: Notification - The created notification
     * Return: Notification
     */
    public Notification add(String username, String content) {
        return addToUser(username, "system", "系统通知", content);
    }

    /**
     * Function: addBroadcast
     * Description: Sends a broadcast notification to all users.
     * Called By: AdminController.sendBroadcast
     * Table Accessed: notifications
     * Table Updated: notifications
     * Input: type (String) - Notification type
     *        title (String) - Notification title
     *        content (String) - Notification body
     * Output: Notification - The created broadcast notification
     * Return: Notification
     */
    public Notification addBroadcast(String type, String title, String content) {
        Notification n = new Notification();
        n.setToUser("*");
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO notifications (to_user, type, title, content, create_time, is_read) VALUES ('*', ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, type);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setLong(4, n.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) n.setId(key.longValue());
        return n;
    }

    /**
     * Function: listByUser
     * Description: Retrieves all notifications for a user, including direct and broadcast messages.
     *              Joins with notification_read table to determine read status for broadcasts.
     * Called By: NotificationController.list
     * Table Accessed: notifications, notification_read
     * Input: username (String) - The username to retrieve notifications for
     * Output: List<Notification> - List of notifications
     * Return: List<Notification>
     */
    public List<Notification> listByUser(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT n.id, n.to_user, n.type, n.title, n.content, n.create_time, n.is_read, nr.username AS read_by " +
                        "FROM notifications n " +
                        "LEFT JOIN notification_read nr ON n.id = nr.notification_id AND nr.username = ? " +
                        "WHERE n.to_user = ? OR n.to_user = '*' " +
                        "ORDER BY n.create_time DESC",
                (rs, rowNum) -> {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setToUser(rs.getString("to_user"));
                    n.setType(rs.getString("type"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    n.setCreateTime(rs.getLong("create_time"));
                    boolean isRead = rs.getInt("is_read") != 0;
                    n.setRead(isRead);
                    String readBy = rs.getString("read_by");
                    if ("*".equals(n.getToUser()) && readBy != null) {
                        java.util.Set<String> s = new java.util.HashSet<>();
                        s.add(username);
                        n.setReadByUsers(s);
                    }
                    return n;
                },
                username,
                username
        );
    }

    /**
     * Function: countUnread
     * Description: Counts unread notifications for a user.
     *              Includes unread direct messages and unread broadcast messages.
     * Called By: NotificationController.countUnread
     * Table Accessed: notifications, notification_read
     * Input: username (String) - The username to count for
     * Output: long - The count of unread notifications
     * Return: long
     */
    public long countUnread(String username) {
        if (username == null) return 0;
        Long direct = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notifications WHERE to_user = ? AND is_read = 0",
                Long.class,
                username
        );
        Long broadcast = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notifications n LEFT JOIN notification_read nr ON n.id = nr.notification_id AND nr.username = ? WHERE n.to_user = '*' AND nr.username IS NULL",
                Long.class,
                username
        );
        long a = direct == null ? 0 : direct;
        long b = broadcast == null ? 0 : broadcast;
        return a + b;
    }

    /**
     * Function: markRead
     * Description: Marks a single notification as read.
     *              For broadcast messages, inserts a record into notification_read.
     *              For direct messages, updates the is_read flag.
     * Called By: NotificationController.markRead
     * Table Accessed: notifications, notification_read
     * Table Updated: notifications, notification_read
     * Input: id (Long) - Notification ID
     *        username (String) - User marking it as read
     * Output: void
     * Return: void
     */
    public void markRead(Long id, String username) {
        if (id == null || username == null) return;
        String toUser;
        try {
            toUser = jdbcTemplate.queryForObject("SELECT to_user FROM notifications WHERE id = ?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return;
        }
        if ("*".equals(toUser)) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO notification_read (notification_id, username, read_time) VALUES (?, ?, ?)",
                    id,
                    username,
                    System.currentTimeMillis()
            );
        } else {
            jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE id = ? AND to_user = ?", id, username);
        }
    }

    /**
     * Function: markAllRead
     * Description: Marks all notifications as read for a user.
     * Called By: NotificationController.markAllRead
     * Table Accessed: notifications, notification_read
     * Table Updated: notifications, notification_read
     * Input: username (String) - User marking all as read
     * Output: void
     * Return: void
     */
    public void markAllRead(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE to_user = ? AND is_read = 0", username);
        jdbcTemplate.update(
                "INSERT IGNORE INTO notification_read (notification_id, username, read_time) " +
                        "SELECT id, ?, ? FROM notifications WHERE to_user = '*'",
                username,
                System.currentTimeMillis()
        );
    }
}
