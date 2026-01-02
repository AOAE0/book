package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.NotificationService;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: OrderController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for order management including creation, payment, cancellation, and receiving.
 * Others:
 * Function List:
 * 1. list - List buyer's orders
 * 2. listForSeller - List seller's orders
 * 3. create - Create a new order
 * 4. pay - Pay for an order
 * 5. cancel - Cancel an order
 * 6. receive - Confirm receipt of an order
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final BookService bookService;
    private final com.whu.bookapi.service.CartService cartService;
    private final NotificationService notificationService;

    public OrderController(OrderService orderService, UserService userService, BookService bookService, com.whu.bookapi.service.CartService cartService, NotificationService notificationService) {
        this.orderService = orderService;
        this.userService = userService;
        this.bookService = bookService;
        this.cartService = cartService;
        this.notificationService = notificationService;
    }

    /**
     * Function: list
     * Description: Lists orders where the current user is the buyer.
     * Calls: UserService.getByToken, OrderService.listByBuyer
     * Called By: Frontend Buyer Order Page
     * Table Accessed: user_token, users, orders
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Order> - List of orders
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Order> orders = orderService.listByBuyer(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    /**
     * Function: listForSeller
     * Description: Lists orders where the current user is the seller.
     * Calls: UserService.getByToken, OrderService.listBySeller
     * Called By: Frontend Seller Order Page
     * Table Accessed: user_token, users, orders
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Order> - List of orders
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/seller/list")
    public ResponseEntity<?> listForSeller(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Order> orders = orderService.listBySeller(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    /**
     * Function: create
     * Description: Creates a new order for a book.
     * Calls: UserService.getByToken, BookService.get, BookService.tryReserveStock, OrderService.create, CartService.remove
     * Called By: Frontend Book Detail / Cart Page
     * Table Accessed: user_token, users, books, orders, cart_items
     * Table Updated: orders, books (stock), cart_items
     * Input: token (String) - User token
     *        bookId (Long) - ID of the book to buy
     * Output: Order - Created order
     * Return: ResponseEntity<?>
     * Others: Checks if user is buying their own book or if stock is available.
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestHeader(value = "token", required = false) String token,
                                    @RequestParam("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Book b = bookService.get(bookId);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!"on_sale".equals(b.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "该教材不可购买"));
        }
        
        if (user.getUsername().equals(b.getSellerName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", "Cannot buy your own book"));
        }

        boolean reserved = bookService.tryReserveStock(bookId);
        if (!reserved) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "库存不足"));
        }
        Order o = orderService.create(b, user.getUsername());
        cartService.remove(user.getUsername(), bookId);
        return ResponseEntity.ok(o);
    }

    /**
     * Function: pay
     * Description: Processes payment for an order.
     * Calls: UserService.getByToken, OrderService.get, OrderService.setStatus, NotificationService.addToUser
     * Called By: Frontend Payment Page
     * Table Accessed: user_token, users, orders
     * Table Updated: orders (status, payment_time), notifications
     * Input: token (String) - User token
     *        id (Long) - Order ID
     * Output: Order - Updated order
     * Return: ResponseEntity<?>
     * Others: Checks for order expiration and valid status.
     */
    @PostMapping("/pay/{id}")
    public ResponseEntity<?> pay(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if ("paid".equals(existing.getStatus())) return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单已支付"));
        if (!"pending".equals(existing.getStatus())) return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可支付"));
        if (existing.getExpireAt() != null && System.currentTimeMillis() > existing.getExpireAt()) {
            orderService.setStatus(id, "expired");
            bookService.releaseStock(existing.getBookId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单已超时"));
        }
        Order o = orderService.setStatus(id, "paid");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        notificationService.addToUser(
                o.getSellerName(),
                "order",
                "订单已付款",
                "订单#" + o.getId() + "（" + o.getBookName() + "）已付款，金额已暂存至平台，买家确认收货后自动结算至您的账户"
        );
        return ResponseEntity.ok(o);
    }

    /**
     * Function: cancel
     * Description: Cancels an order.
     * Calls: UserService.getByToken, OrderService.get, OrderService.setStatus, BookService.releaseStock
     * Called By: Frontend Buyer Order Page
     * Table Accessed: user_token, users, orders
     * Table Updated: orders (status), books (stock)
     * Input: token (String) - User token
     *        id (Long) - Order ID
     * Output: Order - Updated order
     * Return: ResponseEntity<?>
     * Others: Only pending orders can be cancelled.
     */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"pending".equals(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可取消"));
        }
        Order o = orderService.setStatus(id, "cancelled");
        bookService.releaseStock(existing.getBookId());
        return ResponseEntity.ok(o);
    }

    /**
     * Function: receive
     * Description: Confirms receipt of an order by the buyer.
     * Calls: UserService.getByToken, OrderService.get, OrderService.setStatus, NotificationService.addToUser
     * Called By: Frontend Buyer Order Page
     * Table Accessed: user_token, users, orders
     * Table Updated: orders (status, finish_time), notifications
     * Input: token (String) - User token
     *        id (Long) - Order ID
     * Output: Order - Updated order
     * Return: ResponseEntity<?>
     * Others: Only paid orders can be received.
     */
    @PostMapping("/receive/{id}")
    public ResponseEntity<?> receive(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"paid".equals(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可确认收货"));
        }
        Order o = orderService.setStatus(id, "received");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        notificationService.addToUser(
                o.getSellerName(),
                "order",
                "买家已确认收货",
                "订单#" + o.getId() + "（" + o.getBookName() + "）买家已确认收货，金额已打入您的账户，请注意查收"
        );
        return ResponseEntity.ok(o);
    }
}
