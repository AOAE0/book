package com.whu.bookapi.controller;

import com.whu.bookapi.model.ChatMessage;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.ChatService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ChatController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for chat and messaging system.
 * Others:
 * Function List:
 * 1. send - Send a chat message
 * 2. history - Get chat history
 * 3. conversations - List active conversations
 * 4. markRead - Mark messages as read
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    /**
     * Function: send
     * Description: Sends a chat message to another user.
     * Calls: UserService.getByToken, UserService.exists, ChatService.send
     * Called By: Frontend Chat Component
     * Table Accessed: user_token, users, chat_messages
     * Table Updated: chat_messages
     * Input: token (String) - User token
     *        msg (ChatMessage) - Message content
     * Output: ChatMessage - Sent message
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "token", required = false) String token,
                                  @RequestBody ChatMessage msg) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (msg.getToUser() == null || !userService.exists(msg.getToUser())) {
            return ResponseEntity.badRequest().body("Target user does not exist");
        }
        msg.setFromUser(u.getUsername());
        return ResponseEntity.ok(chatService.send(msg));
    }

    /**
     * Function: history
     * Description: Retrieves chat history with a specific peer.
     * Calls: UserService.getByToken, ChatService.history
     * Called By: Frontend Chat Component
     * Table Accessed: user_token, users, chat_messages
     * Table Updated: None
     * Input: token (String) - User token
     *        peer (String) - Peer username
     *        bookId (Long) - Filter by book context (optional)
     *        orderId (Long) - Filter by order context (optional)
     * Output: List<ChatMessage> - List of messages
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestHeader(value = "token", required = false) String token,
                                     @RequestParam("peer") String peer,
                                     @RequestParam(value = "bookId", required = false) Long bookId,
                                     @RequestParam(value = "orderId", required = false) Long orderId) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<ChatMessage> list = chatService.history(u.getUsername(), peer, bookId, orderId);
        return ResponseEntity.ok(list);
    }

    /**
     * Function: conversations
     * Description: Lists all active conversations for the current user.
     * Calls: UserService.getByToken, ChatService.listConversations
     * Called By: Frontend Message Center
     * Table Accessed: user_token, users, chat_messages
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of conversations
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> conversations(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<java.util.Map<String, Object>> list = chatService.listConversations(u.getUsername());
        return ResponseEntity.ok(list);
    }

    /**
     * Function: markRead
     * Description: Marks all messages from a specific peer as read.
     * Calls: UserService.getByToken, ChatService.markRead
     * Called By: Frontend Chat Component
     * Table Accessed: user_token, users, chat_messages
     * Table Updated: chat_messages
     * Input: token (String) - User token
     *        peer (String) - Peer username
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/read/{peer}")
    public ResponseEntity<?> markRead(@RequestHeader(value = "token", required = false) String token,
                                      @PathVariable("peer") String peer) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        chatService.markRead(u.getUsername(), peer);
        return ResponseEntity.ok().build();
    }
}
