/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: OrderScheduler.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Scheduled tasks for order management.
 *              Handles automatic cancellation of expired orders.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi.schedule;

import com.whu.bookapi.model.Order;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled task for order maintenance.
 */
@Component
public class OrderScheduler {
    private final OrderService orderService;
    private final BookService bookService;

    public OrderScheduler(OrderService orderService, BookService bookService) {
        this.orderService = orderService;
        this.bookService = bookService;
    }

    /**
     * Function: autoCancelPending
     * Description: Automatically cancels pending orders that have expired.
     *              Runs every 60 seconds.
     * Logic:
     * 1. List all pending orders.
     * 2. Check if current time > expiration time.
     * 3. If expired, set status to "expired" and release book stock.
     */
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPending() {
        List<Order> all = orderService.listAll();
        long now = System.currentTimeMillis();
        for (Order o : all) {
            if ("pending".equals(o.getStatus())) {
                Long expireAt = o.getExpireAt();
                if (expireAt != null && expireAt > 0 && now > expireAt) {
                    orderService.setStatus(o.getId(), "expired");
                    bookService.releaseStock(o.getBookId());
                }
            }
        }
    }
}
