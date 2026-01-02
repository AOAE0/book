package com.whu.bookapi.controller;

import com.whu.bookapi.model.CartItem;
import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.CartService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: CartController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for shopping cart management.
 * Others:
 * Function List:
 * 1. list - List items in the cart
 * 2. add - Add an item to the cart
 * 3. remove - Remove an item from the cart
 * 4. clear - Clear the cart
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final BookService bookService;

    public CartController(CartService cartService, UserService userService, BookService bookService) {
        this.cartService = cartService;
        this.userService = userService;
        this.bookService = bookService;
    }

    /**
     * Function: list
     * Description: Lists items in the current user's shopping cart.
     * Calls: UserService.getByToken, CartService.list, BookService.get
     * Called By: Frontend Cart Page
     * Table Accessed: user_token, users, cart_items, books
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of cart items with book details
     * Return: ResponseEntity<?>
     * Others: Enriches cart items with book details.
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<CartItem> list = cartService.list(user.getUsername());
        java.util.List<java.util.Map<String, Object>> view = new java.util.ArrayList<>();
        for (CartItem ci : list) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("bookId", ci.getBookId());
            m.put("quantity", ci.getQuantity());
            Book b = bookService.get(ci.getBookId());
            if (b != null) {
                m.put("bookName", b.getBookName());
                m.put("sellPrice", b.getSellPrice());
                m.put("coverUrl", b.getCoverUrl());
                m.put("sellerName", b.getSellerName());
            }
            view.add(m);
        }
        return ResponseEntity.ok(view);
    }

    /**
     * Function: add
     * Description: Adds a book to the shopping cart.
     * Calls: UserService.getByToken, CartService.add
     * Called By: Frontend Book Detail Page
     * Table Accessed: user_token, users, cart_items
     * Table Updated: cart_items
     * Input: token (String) - User token
     *        bookId (Long) - ID of the book to add
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            cartService.add(user.getUsername(), bookId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Function: remove
     * Description: Removes a book from the shopping cart.
     * Calls: UserService.getByToken, CartService.remove
     * Called By: Frontend Cart Page
     * Table Accessed: user_token, users, cart_items
     * Table Updated: cart_items
     * Input: token (String) - User token
     *        bookId (Long) - ID of the book to remove
     *        count (Integer) - Number of items to remove (optional)
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> remove(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("bookId") Long bookId,
                                    @RequestParam(value = "count", required = false) Integer count) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        cartService.remove(user.getUsername(), bookId, count);
        return ResponseEntity.ok().build();
    }

    /**
     * Function: clear
     * Description: Clears all items from the shopping cart.
     * Calls: UserService.getByToken, CartService.clear
     * Called By: Frontend Cart Page
     * Table Accessed: user_token, users, cart_items
     * Table Updated: cart_items
     * Input: token (String) - User token
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        cartService.clear(user.getUsername());
        return ResponseEntity.ok().build();
    }
}
