package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.model.Complaint;
import com.whu.bookapi.model.OperationLog;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.ReviewService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: AdminController.java
 * Author: WiseBookPal Team Version: 1.1 Date: 2026-01-02
 * Description: Controller for administrator operations including user management,
 *              book review, seller application processing, and system announcements.
 * Others:
 * Function List:
 * 1. listSellerApplications - Lists pending seller applications.
 * 2. approveSeller - Approves a seller application.
 * 3. rejectSeller - Rejects a seller application.
 * 4. listUnderReview - Lists books under review.
 * 5. approve - Approves a book listing.
 * 6. reject - Rejects a book listing.
 * 7. listUsers - Searches for users (U14).
 * 8. setRole - Sets user role.
 * 9. deleteUser - Deletes a user (U14).
 * 10. getUserDetail - Gets user details (U14).
 * 11. updateUserStatus - Updates user status (U14).
 * 12. undoBlacklist - Undoes blacklist (U14).
 * 13. updateUserInfo - Updates user info (U14).
 * 14. getOperationLogs - Gets operation logs (U14).
 * 15. listComplaints - Lists complaints.
 * 16. approveComplaint - Approves complaint.
 * 17. rejectComplaint - Rejects complaint.
 * 18. announce - Sends system announcement.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added U14 User Management features
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final BookService bookService;
    private final ComplaintService complaintService;
    private final NotificationService notificationService;
    private final ReviewService reviewService;

    public AdminController(UserService userService, BookService bookService, ComplaintService complaintService, NotificationService notificationService, ReviewService reviewService) {
        this.userService = userService;
        this.bookService = bookService;
        this.complaintService = complaintService;
        this.notificationService = notificationService;
        this.reviewService = reviewService;
    }

    /**
     * Function: listSellerApplications
     * Description: Lists pending seller applications for admin review.
     * Calls: UserService.getByToken, UserService.listSellerApplications
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: None
     * Input: token (String) - Admin token
     * Output: List<User> - List of pending applicants
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/seller-applications")
    public ResponseEntity<?> listSellerApplications(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.listSellerApplications());
    }

    /**
     * Function: approveSeller
     * Description: Approves a seller application.
     * Calls: UserService.approveSeller, NotificationService.add
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users (seller_status)
     * Input: token (String) - Admin token
     *        username (String) - Applicant username
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/approve-seller/{username}")
    public ResponseEntity<?> approveSeller(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.approveSeller(username);
        notificationService.add(username, "恭喜，您的卖家资质申请已通过！");
        return ResponseEntity.ok().build();
    }

    /**
     * Function: rejectSeller
     * Description: Rejects a seller application.
     * Calls: UserService.rejectSeller, NotificationService.add
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users (seller_status)
     * Input: token (String) - Admin token
     *        username (String) - Applicant username
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/reject-seller/{username}")
    public ResponseEntity<?> rejectSeller(@RequestHeader(value = "token", required = false) String token,
                                          @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.rejectSeller(username);
        notificationService.add(username, "很遗憾，您的卖家资质申请未通过。");
        return ResponseEntity.ok().build();
    }

    /**
     * Function: isAdmin
     * Description: Checks if the user has admin role.
     * Calls: None
     * Called By: AdminController methods
     * Table Accessed: None
     * Table Updated: None
     * Input: u (User) - User object
     * Output: boolean - True if admin
     * Return: boolean
     * Others:
     */
    private boolean isAdmin(User u) {
        return u != null && "admin".equals(u.getRole());
    }

    /**
     * Function: listUnderReview
     * Description: Lists books that are under review.
     * Calls: BookService.listUnderReview
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, books
     * Table Updated: None
     * Input: token (String) - Admin token
     * Output: List<Book> - Books under review
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/review/books")
    public ResponseEntity<?> listUnderReview(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Book> list = bookService.listUnderReview();
        return ResponseEntity.ok(list);
    }

    /**
     * Function: approve
     * Description: Approves a book listing.
     * Calls: BookService.approve, NotificationService.addToUser
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, books
     * Table Updated: books (status)
     * Input: token (String) - Admin token
     *        id (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/review/books/{id}/approve")
    public ResponseEntity<?> approve(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.approve(id);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // 通知卖家审核结果
        com.whu.bookapi.model.Book b = bookService.get(id);
        if (b != null) {
            notificationService.addToUser(b.getSellerName(), "audit", "教材审核通过", "您的教材《" + b.getBookName() + "》已通过审核");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: reject
     * Description: Rejects a book listing.
     * Calls: BookService.reject, NotificationService.addToUser
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, books
     * Table Updated: books (status)
     * Input: token (String) - Admin token
     *        id (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/review/books/{id}/reject")
    public ResponseEntity<?> reject(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.reject(id);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        com.whu.bookapi.model.Book b = bookService.get(id);
        if (b != null) {
            notificationService.addToUser(b.getSellerName(), "audit", "教材审核未通过", "您的教材《" + b.getBookName() + "》未通过审核");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: listUsers
     * Description: Searches for users based on a keyword (U14).
     * Calls: UserService.searchUsers
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: None
     * Input: token (String) - Admin token
     *        keyword (String) - Search keyword (username/phone/studentId)
     * Output: List<User> - Matching users
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader(value = "token", required = false) String token,
                                       @RequestParam(value = "keyword", required = false) String keyword) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    /**
     * Function: setRole
     * Description: Sets the role of a user.
     * Calls: UserService.setUserRole
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, user_roles
     * Table Updated: user_roles
     * Input: token (String) - Admin token
     *        username (String) - Target user
     *        role (String) - New role
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/users/{username}/role")
    public ResponseEntity<?> setRole(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("username") String username,
                                     @RequestParam("role") String role) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = userService.setUserRole(username, role);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: deleteUser
     * Description: Deletes a user account (U14).
     * Calls: UserService.deleteUser
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users (status)
     * Input: token (String) - Admin token
     *        username (String) - Target user
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@RequestHeader(value = "token", required = false) String token,
                                        @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = userService.deleteUser(username, u.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: getUserDetail
     * Description: Retrieves detailed information about a user (U14).
     * Calls: UserService.getUserDetail, UserService.listAddresses
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: None
     * Input: token (String) - Admin token
     *        username (String) - Target user
     * Output: Map - User details and addresses
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserDetail(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User detail = userService.getUserDetail(username);
        if (detail == null) return ResponseEntity.notFound().build();
        // Enrich with addresses?
        List<Map<String, Object>> addresses = userService.listAddresses(username);
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("user", detail);
        res.put("addresses", addresses);
        return ResponseEntity.ok(res);
    }

    /**
     * Function: updateUserStatus
     * Description: Updates the status of a user (normal, blacklist, etc.) (U14).
     * Calls: UserService.updateUserStatus
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users (status), operation_logs
     * Input: token (String) - Admin token
     *        username (String) - Target user
     *        body (Map) - Status, reason, second admin credentials
     * Output: Map - Result of operation
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/users/{username}/status")
    public ResponseEntity<?> updateUserStatus(@RequestHeader(value = "token", required = false) String token,
                                              @PathVariable("username") String username,
                                              @RequestBody Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String status = body.get("status");
        String reason = body.get("reason");
        String secondAdmin = body.get("secondAdmin");
        String secondAdminPwd = body.get("secondAdminPwd");
        
        Map<String, Object> result = userService.updateUserStatus(username, status, reason, u.getUsername(), secondAdmin, secondAdminPwd);
        return ResponseEntity.ok(result);
    }

    /**
     * Function: undoBlacklist
     * Description: Reverts a blacklist operation within 24 hours (U14).
     * Calls: UserService.undoBlacklist
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users (status), operation_logs
     * Input: token (String) - Admin token
     *        username (String) - Target user
     *        body (Map) - Reason
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/users/{username}/undo-blacklist")
    public ResponseEntity<?> undoBlacklist(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username,
                                           @RequestBody(required = false) Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String reason = body == null ? null : body.get("reason");
        boolean ok = userService.undoBlacklist(username, u.getUsername(), reason);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Undo failed or time expired"));
        return ResponseEntity.ok().build();
    }

    /**
     * Function: updateUserInfo
     * Description: Updates user information by admin (U14).
     * Calls: UserService.updateUserInfo
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: users
     * Input: token (String) - Admin token
     *        username (String) - Target user
     *        body (User) - Updated user info
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/users/{username}/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader(value = "token", required = false) String token,
                                            @PathVariable("username") String username,
                                            @RequestBody User body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        body.setUsername(username); // ensure username matches path
        boolean ok = userService.updateUserInfo(body, u.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: getOperationLogs
     * Description: Retrieves system operation logs with filters (U14).
     * Calls: UserService.getOperationLogs
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, operation_logs
     * Table Updated: None
     * Input: token (String) - Admin token
     *        keyword (String) - Search keyword
     *        targetUser (String) - Filter by target user
     *        operator (String) - Filter by operator
     *        startTime (Long) - Filter by start time
     *        endTime (Long) - Filter by end time
     *        action (String) - Filter by action type
     * Output: List<OperationLog> - Matching logs
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/users/logs")
    public ResponseEntity<?> getOperationLogs(@RequestHeader(value = "token", required = false) String token,
                                              @RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "targetUser", required = false) String targetUser,
                                              @RequestParam(value = "operator", required = false) String operator,
                                              @RequestParam(value = "startTime", required = false) Long startTime,
                                              @RequestParam(value = "endTime", required = false) Long endTime,
                                              @RequestParam(value = "action", required = false) String action) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<OperationLog> logs = userService.getOperationLogs(keyword, targetUser, operator, startTime, endTime, action);
        return ResponseEntity.ok(logs);
    }

    /**
     * Function: listComplaints
     * Description: Lists all complaints.
     * Calls: ComplaintService.listAll
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users, complaints
     * Table Updated: None
     * Input: token (String) - Admin token
     * Output: List<Complaint> - List of complaints
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/complaints")
    public ResponseEntity<?> listComplaints(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Complaint> list = complaintService.listAll();
        return ResponseEntity.ok(list);
    }

    /**
     * Function: approveComplaint
     * Description: Approves a complaint.
     * Calls: ComplaintService.setStatus, NotificationService.addToUser
     * Input: token (String) - Admin token
     *        id (Long) - Complaint ID
     * Output: None
     * Return: ResponseEntity<?>
     */
    @PostMapping("/complaints/{id}/approve")
    public ResponseEntity<?> approveComplaint(@RequestHeader(value = "token", required = false) String token,
                                              @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = complaintService.setStatus(id, "approved");
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Complaint c = null;
        for (Complaint cc : complaintService.listAll()) if (cc.getId().equals(id)) { c = cc; break; }
        if (c != null) {
            notificationService.addToUser(c.getUsername(), "complaint", "投诉审核通过", "您对订单" + c.getOrderId() + "的投诉已通过");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: rejectComplaint
     * Description: Rejects a complaint.
     * Calls: ComplaintService.setStatus, NotificationService.addToUser
     * Input: token (String) - Admin token
     *        id (Long) - Complaint ID
     * Output: None
     * Return: ResponseEntity<?>
     */
    @PostMapping("/complaints/{id}/reject")
    public ResponseEntity<?> rejectComplaint(@RequestHeader(value = "token", required = false) String token,
                                             @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = complaintService.setStatus(id, "rejected");
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Complaint c = null;
        for (Complaint cc : complaintService.listAll()) if (cc.getId().equals(id)) { c = cc; break; }
        if (c != null) {
            notificationService.addToUser(c.getUsername(), "complaint", "投诉审核未通过", "您对订单" + c.getOrderId() + "的投诉未通过");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Function: announce
     * Description: Sends a system-wide announcement.
     * Calls: NotificationService.addBroadcast
     * Called By: Frontend Admin Dashboard
     * Table Accessed: user_token, users
     * Table Updated: notifications
     * Input: token (String) - Admin token
     *        body (Map) - Title and content
     * Output: Map - Success status
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/announce")
    public ResponseEntity<?> announce(@RequestHeader(value = "token", required = false) String token,
                                      @RequestBody java.util.Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String title = body.getOrDefault("title", "平台公告");
        String content = body.getOrDefault("content", "");
        notificationService.addBroadcast("announcement", title, content);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }

    /**
     * Function: listPendingReviews
     * Description: Lists pending reviews for moderation.
     * Calls: ReviewService.listPending
     * Input: token (String) - Admin token
     * Output: List<Review> - Pending reviews
     * Return: ResponseEntity<?>
     */
    @GetMapping("/reviews/pending")
    public ResponseEntity<?> listPendingReviews(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reviewService.listPending());
    }

    /**
     * Function: auditReview
     * Description: Audits a review (approve/reject).
     * Calls: ReviewService.audit
     * Input: token (String) - Admin token
     *        id (Long) - Review ID
     *        body (Map) - Status and reason
     * Output: None
     * Return: ResponseEntity<?>
     */
    @PostMapping("/reviews/{id}/audit")
    public ResponseEntity<?> auditReview(@RequestHeader(value = "token", required = false) String token,
                                         @PathVariable("id") Long id,
                                         @RequestBody java.util.Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String status = body.get("status");
        String reason = body.get("reason");
        boolean ok = reviewService.audit(id, status, reason);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: undoAuditReview
     * Description: Reverts a review audit decision.
     * Calls: ReviewService.undoAudit
     * Input: token (String) - Admin token
     *        id (Long) - Review ID
     * Output: None
     * Return: ResponseEntity<?>
     */
    @PostMapping("/reviews/{id}/undo")
    public ResponseEntity<?> undoAuditReview(@RequestHeader(value = "token", required = false) String token,
                                             @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = reviewService.undoAudit(id);
        if (!ok) return ResponseEntity.badRequest().body("Undo failed");
        return ResponseEntity.ok().build();
    }
}
