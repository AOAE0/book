package com.whu.bookapi.controller;

import com.whu.bookapi.model.Complaint;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ComplaintController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for handling user complaints.
 * Others:
 * Function List:
 * 1. add - Submit a new complaint
 * 2. my - List my complaints
 * 3. received - List complaints received against me (seller)
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;
    private final UserService userService;
    private final com.whu.bookapi.service.OrderService orderService;

    public ComplaintController(ComplaintService complaintService, UserService userService, com.whu.bookapi.service.OrderService orderService) {
        this.complaintService = complaintService;
        this.userService = userService;
        this.orderService = orderService;
    }

    /**
     * Function: add
     * Description: Submits a new complaint.
     * Calls: UserService.getByToken, ComplaintService.add
     * Called By: Frontend Order Page / User Profile
     * Table Accessed: user_token, users, complaints
     * Table Updated: complaints
     * Input: token (String) - User token
     *        complaint (Complaint) - Complaint details
     * Output: Complaint - Created complaint
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Complaint complaint) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        complaint.setUsername(u.getUsername());
        return ResponseEntity.ok(complaintService.add(complaint));
    }

    /**
     * Function: my
     * Description: Lists complaints submitted by the current user.
     * Calls: UserService.getByToken, ComplaintService.listByUser
     * Called By: Frontend Complaint Center
     * Table Accessed: user_token, users, complaints
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Complaint> - List of complaints
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/my")
    public ResponseEntity<?> my(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(complaintService.listByUser(u.getUsername()));
    }

    /**
     * Function: received
     * Description: Lists complaints received by the current user (as a seller).
     * Calls: UserService.getByToken, ComplaintService.listAll, OrderService.get
     * Called By: Frontend Complaint Center
     * Table Accessed: user_token, users, complaints, orders
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of received complaints with details
     * Return: ResponseEntity<?>
     * Others: Filters all complaints to find those related to orders where the user is the seller.
     */
    @GetMapping("/received")
    public ResponseEntity<?> received(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Complaint> all = complaintService.listAll();
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (Complaint c : all) {
            com.whu.bookapi.model.Order o = orderService.get(c.getOrderId());
            if (o != null && u.getUsername().equals(o.getSellerName())) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", c.getId());
                m.put("orderId", c.getOrderId());
                m.put("buyerName", c.getUsername());
                m.put("type", c.getType());
                m.put("detail", c.getDetail());
                m.put("createTime", c.getCreateTime());
                m.put("status", c.getStatus());
                res.add(m);
            }
        }
        return ResponseEntity.ok(res);
    }
}
