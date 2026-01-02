package com.whu.bookapi.service;

import com.whu.bookapi.model.Review;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ReviewService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Service for managing book reviews and ratings.
 *              Handles review submission, drafting, and moderation.
 * Others:
 * Function List:
 * 1. add - Submits a new review.
 * 2. listByUser - Retrieves reviews by a specific user.
 * 3. saveDraft - Saves a review draft.
 * 4. getDraft - Retrieves a saved draft.
 * 5. listAll - Lists all reviews (for admin).
 * 6. listPending - Lists pending reviews (for moderation).
 * 7. audit - Approves or rejects a review.
 * 8. undoAudit - Reverts a moderation decision within 24 hours.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added moderation and undo functionality
 */
@Service
public class ReviewService {
    private final JdbcTemplate jdbcTemplate;

    private static final java.util.Set<String> SENSITIVE = new java.util.HashSet<>(java.util.Arrays.asList("辱骂", "骚扰", "黄赌毒", "垃圾", "骗子"));

    public ReviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: filter
     * Description: Filters sensitive words from text.
     * Input: s (String) - Input text
     * Output: String - Filtered text
     */
    private String filter(String s) {
        if (s == null) return null;
        String r = s;
        for (String w : SENSITIVE) {
            r = r.replace(w, "**");
        }
        return r;
    }

    /**
     * Function: tagsToText
     * Description: Converts a list of tags to a comma-separated string.
     * Input: tags (List<String>) - List of tags
     * Output: String - Comma-separated string
     */
    private static String tagsToText(java.util.List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        java.util.List<String> cleaned = new java.util.ArrayList<>();
        for (String t : tags) {
            if (t == null) continue;
            String tt = t.trim();
            if (!tt.isEmpty()) cleaned.add(tt);
        }
        if (cleaned.isEmpty()) return null;
        return String.join(",", cleaned);
    }

    /**
     * Function: textToTags
     * Description: Converts a comma-separated string to a list of tags.
     * Input: text (String) - Comma-separated string
     * Output: List<String> - List of tags
     */
    private static java.util.List<String> textToTags(String text) {
        if (text == null || text.isEmpty()) return null;
        String[] parts = text.split(",");
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) tags.add(t);
        }
        return tags.isEmpty() ? null : tags;
    }

    /**
     * Function: add
     * Description: Adds a new review to the database.
     * Calls: filter, tagsToText
     * Called By: ReviewController.add
     * Table Accessed: reviews
     * Table Updated: reviews
     * Input: r (Review) - Review object
     * Output: Review - Created review with ID
     * Return: Review
     */
    public Review add(Review r) {
        if (r == null || r.getOrderId() == null || r.getUsername() == null) return null;
        r.setCreateTime(System.currentTimeMillis());
        r.setComment(filter(r.getComment()));
        String tags = tagsToText(r.getTags());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reviews (order_id, username, score_condition, score_service, comment, tags, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, r.getOrderId());
            ps.setString(2, r.getUsername());
            ps.setInt(3, r.getScoreCondition());
            ps.setInt(4, r.getScoreService());
            ps.setString(5, r.getComment());
            ps.setString(6, tags);
            ps.setLong(7, r.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) r.setId(key.longValue());
        return r;
    }

    /**
     * Function: listByUser
     * Description: Retrieves all reviews submitted by a user.
     * Calls: textToTags
     * Called By: ReviewController.listByUser
     * Table Accessed: reviews
     * Input: username (String) - User's username
     * Output: List<Review> - List of reviews
     * Return: List<Review>
     */
    public List<Review> listByUser(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT id, order_id, username, score_condition, score_service, comment, tags, create_time FROM reviews WHERE username = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setUsername(rs.getString("username"));
                    r.setScoreCondition(rs.getInt("score_condition"));
                    r.setScoreService(rs.getInt("score_service"));
                    r.setComment(rs.getString("comment"));
                    r.setTags(textToTags(rs.getString("tags")));
                    r.setCreateTime(rs.getLong("create_time"));
                    return r;
                },
                username
        );
    }

    /**
     * Function: saveDraft
     * Description: Saves or updates a review draft.
     * Calls: tagsToText
     * Called By: ReviewController.saveDraft
     * Table Accessed: review_draft
     * Table Updated: review_draft
     * Input: username (String) - User's username
     *        r (Review) - Review draft
     * Output: Review - Saved draft
     * Return: Review
     */
    public Review saveDraft(String username, Review r) {
        if (r.getOrderId() == null) return null;
        Review copy = new Review();
        copy.setOrderId(r.getOrderId());
        copy.setUsername(username);
        copy.setScoreCondition(r.getScoreCondition());
        copy.setScoreService(r.getScoreService());
        copy.setComment(r.getComment());
        copy.setTags(r.getTags());
        copy.setCreateTime(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO review_draft (username, order_id, score_condition, score_service, comment, tags, create_time) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE score_condition=VALUES(score_condition), score_service=VALUES(score_service), comment=VALUES(comment), tags=VALUES(tags), create_time=VALUES(create_time)",
                username,
                r.getOrderId(),
                r.getScoreCondition(),
                r.getScoreService(),
                r.getComment(),
                tagsToText(r.getTags()),
                copy.getCreateTime()
        );
        return copy;
    }

    /**
     * Function: getDraft
     * Description: Retrieves a saved review draft.
     * Calls: textToTags
     * Called By: ReviewController.getDraft
     * Table Accessed: review_draft
     * Input: username (String) - User's username
     *        orderId (Long) - Order ID
     * Output: Review - Draft if exists, else null
     * Return: Review
     */
    public Review getDraft(String username, Long orderId) {
        if (orderId == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT username, order_id, score_condition, score_service, comment, tags, create_time FROM review_draft WHERE username = ? AND order_id = ?",
                    (rs, rowNum) -> {
                        Review r = new Review();
                        r.setOrderId(rs.getLong("order_id"));
                        r.setUsername(rs.getString("username"));
                        r.setScoreCondition(rs.getInt("score_condition"));
                        r.setScoreService(rs.getInt("score_service"));
                        r.setComment(rs.getString("comment"));
                        r.setTags(textToTags(rs.getString("tags")));
                        r.setCreateTime(rs.getLong("create_time"));
                        return r;
                    },
                    username,
                    orderId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Function: listAll
     * Description: Lists all reviews for admin, including status.
     * Calls: textToTags
     * Called By: AdminController.listReviews
     * Table Accessed: reviews
     * Input: None
     * Output: List<Review> - List of all reviews
     * Return: List<Review>
     */
    public java.util.List<Review> listAll() {
        return jdbcTemplate.query(
                "SELECT id, order_id, username, score_condition, score_service, comment, tags, create_time, status, audit_reason, audit_time FROM reviews ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setUsername(rs.getString("username"));
                    r.setScoreCondition(rs.getInt("score_condition"));
                    r.setScoreService(rs.getInt("score_service"));
                    r.setComment(rs.getString("comment"));
                    r.setTags(textToTags(rs.getString("tags")));
                    r.setCreateTime(rs.getLong("create_time"));
                    r.setStatus(rs.getString("status"));
                    r.setAuditReason(rs.getString("audit_reason"));
                    r.setAuditTime(rs.getObject("audit_time") == null ? null : rs.getLong("audit_time"));
                    return r;
                }
        );
    }

    /**
     * Function: listPending
     * Description: Lists reviews waiting for moderation.
     * Calls: textToTags
     * Called By: AdminController.listPendingReviews
     * Table Accessed: reviews
     * Input: None
     * Output: List<Review> - List of pending reviews
     * Return: List<Review>
     */
    public List<Review> listPending() {
        return jdbcTemplate.query(
                "SELECT id, order_id, username, score_condition, score_service, comment, tags, create_time, status FROM reviews WHERE status = 'pending' ORDER BY create_time ASC",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setUsername(rs.getString("username"));
                    r.setScoreCondition(rs.getInt("score_condition"));
                    r.setScoreService(rs.getInt("score_service"));
                    r.setComment(rs.getString("comment"));
                    r.setTags(textToTags(rs.getString("tags")));
                    r.setCreateTime(rs.getLong("create_time"));
                    r.setStatus(rs.getString("status"));
                    return r;
                }
        );
    }

    /**
     * Function: audit
     * Description: Updates the moderation status of a review.
     * Called By: AdminController.auditReview
     * Table Accessed: reviews
     * Table Updated: reviews
     * Input: id (Long) - Review ID
     *        status (String) - New status (approved/rejected)
     *        reason (String) - Reason for decision
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean audit(Long id, String status, String reason) {
        if (id == null || status == null) return false;
        long now = System.currentTimeMillis();
        int updated = jdbcTemplate.update(
                "UPDATE reviews SET status = ?, audit_reason = ?, audit_time = ? WHERE id = ?",
                status, reason, now, id
        );
        return updated > 0;
    }

    /**
     * Function: undoAudit
     * Description: Reverts a moderation decision if within 24 hours.
     * Called By: AdminController.undoReviewAudit
     * Table Accessed: reviews
     * Table Updated: reviews
     * Input: id (Long) - Review ID
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean undoAudit(Long id) {
        if (id == null) return false;
        long now = System.currentTimeMillis();
        long limit = now - 24 * 60 * 60 * 1000L;
        int updated = jdbcTemplate.update(
                "UPDATE reviews SET status = 'pending', audit_reason = NULL, audit_time = NULL WHERE id = ? AND audit_time >= ?",
                id, limit
        );
        return updated > 0;
    }
}
