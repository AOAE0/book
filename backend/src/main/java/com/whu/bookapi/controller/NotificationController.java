package com.whu.bookapi.controller;

import com.whu.bookapi.model.Notification;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.NotificationService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: NotificationController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for managing system notifications and announcements.
 * Others:
 * Function List:
 * 1. announce - Send a system announcement (Admin only)
 * 2. list - List user notifications
 * 3. unreadCount - Get count of unread notifications and chat messages
 * 4. markRead - Mark a notification as read
 * 5. markAllRead - Mark all notifications as read
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;
    private final com.whu.bookapi.service.ChatService chatService;

    public NotificationController(NotificationService notificationService, UserService userService, com.whu.bookapi.service.ChatService chatService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.chatService = chatService;
    }

    /**
     * Function: announce
     * Description: Sends a system-wide announcement to all users (broadcast).
     * Calls: UserService.getByToken, NotificationService.addBroadcast
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, notifications
     * Table Updated: notifications
     * Input: token (String) - Admin token
     *        body (Map) - Title and content of announcement
     * Output: None
     * Return: ResponseEntity<?>
     * Others: Restricted to admin users.
     */
    @PostMapping("/announce")
    public ResponseEntity<?> announce(@RequestHeader(value = "token", required = false) String token,
                                      @RequestBody Map<String, String> body) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || content == null) return ResponseEntity.badRequest().build();
        
        // Use broadcast mechanism instead of iterating users
        notificationService.addBroadcast("SYSTEM", title, content);
        return ResponseEntity.ok().build();
    }

    /**
     * Function: list
     * Description: Lists notifications for the current user.
     * Calls: UserService.getByToken, NotificationService.listByUser
     * Called By: Frontend Message Center
     * Table Accessed: user_token, users, notifications
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of notifications
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Notification> list = notificationService.listByUser(u.getUsername());
        
        List<Map<String, Object>> dtos = new java.util.ArrayList<>();
        for (Notification n : list) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("createTime", n.getCreateTime());
            map.put("read", n.isReadBy(u.getUsername()));
            dtos.add(map);
        }
        return ResponseEntity.ok(dtos);
    }

    /**
     * Function: unreadCount
     * Description: Gets the total count of unread notifications and chat messages.
     * Calls: UserService.getByToken, NotificationService.countUnread, ChatService.countTotalUnread
     * Called By: Frontend Page Header (Badge)
     * Table Accessed: user_token, users, notifications, chat_messages
     * Table Updated: None
     * Input: token (String) - User token
     * Output: Map - {"count": total_unread}
     * Return: ResponseEntity<?>
     * Others: Combines system notifications and chat messages.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long notifCount = notificationService.countUnread(u.getUsername());
        long chatCount = chatService.countTotalUnread(u.getUsername());
        Map<String, Object> m = new HashMap<>();
        m.put("count", notifCount + chatCount);
        return ResponseEntity.ok(m);
    }

    /**
     * Function: markRead
     * Description: Marks a specific notification as read.
     * Calls: UserService.getByToken, NotificationService.markRead
     * Called By: Frontend Message Center
     * Table Accessed: user_token, users, notifications
     * Table Updated: notifications (read_status)
     * Input: token (String) - User token
     *        id (Long) - Notification ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@RequestHeader(value = "token", required = false) String token,
                                      @PathVariable Long id) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        notificationService.markRead(id, u.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Function: markAllRead
     * Description: Marks all notifications for the user as read.
     * Calls: UserService.getByToken, NotificationService.markAllRead
     * Called By: Frontend Message Center
     * Table Accessed: user_token, users, notifications
     * Table Updated: notifications (read_status)
     * Input: token (String) - User token
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        notificationService.markAllRead(u.getUsername());
        return ResponseEntity.ok().build();
    }
}
