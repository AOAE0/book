package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.FavoriteService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: FavoriteController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for managing user favorites (book collection).
 * Others:
 * Function List:
 * 1. list - List favorite books
 * 2. check - Check if a book is favorited
 * 3. listIds - List IDs of favorite books
 * 4. add - Add a book to favorites
 * 5. remove - Remove a book from favorites
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    /**
     * Function: list
     * Description: Lists all books favorited by the current user.
     * Calls: UserService.getByToken, FavoriteService.list
     * Called By: Frontend Favorites Page
     * Table Accessed: user_token, users, favorites, books
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Book> - List of favorite books
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Book> list = favoriteService.list(user.getUsername());
        return ResponseEntity.ok(list);
    }

    /**
     * Function: check
     * Description: Checks if a specific book is in the user's favorites.
     * Calls: UserService.getByToken, FavoriteService.isCollected
     * Called By: Frontend Book Detail Page
     * Table Accessed: user_token, users, favorites
     * Table Updated: None
     * Input: token (String) - User token
     *        bookId (Long) - Book ID
     * Output: Map - {"collected": boolean}
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/check/{bookId}")
    public ResponseEntity<?> check(@RequestHeader(value = "token", required = false) String token,
                                   @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean collected = favoriteService.isCollected(user.getUsername(), bookId);
        return ResponseEntity.ok(Collections.singletonMap("collected", collected));
    }

    /**
     * Function: listIds
     * Description: Lists IDs of all books favorited by the current user.
     * Calls: UserService.getByToken, FavoriteService.listIds
     * Called By: Frontend Book List Page
     * Table Accessed: user_token, users, favorites
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Long> - List of book IDs
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/ids")
    public ResponseEntity<?> listIds(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Long> ids = favoriteService.listIds(user.getUsername());
        return ResponseEntity.ok(ids);
    }

    /**
     * Function: add
     * Description: Adds a book to the user's favorites.
     * Calls: UserService.getByToken, FavoriteService.add
     * Called By: Frontend Book Detail Page
     * Table Accessed: user_token, users, favorites
     * Table Updated: favorites
     * Input: token (String) - User token
     *        bookId (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        favoriteService.add(user.getUsername(), bookId);
        return ResponseEntity.ok().build();
    }

    /**
     * Function: remove
     * Description: Removes a book from the user's favorites.
     * Calls: UserService.getByToken, FavoriteService.remove
     * Called By: Frontend Book Detail Page / Favorites Page
     * Table Accessed: user_token, users, favorites
     * Table Updated: favorites
     * Input: token (String) - User token
     *        bookId (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     * Others:
     */
    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> remove(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        favoriteService.remove(user.getUsername(), bookId);
        return ResponseEntity.ok().build();
    }
}
