package com.whu.bookapi.controller;

import com.whu.bookapi.dto.PageResponse;
import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * FileName: BookController.java
 * Author: WiseBookPal Team
 * Version: 1.0
 * Date: 2024-11-20
 * Description: Controller for book-related operations including searching, adding, updating, and deleting books.
 * History:
 * <author>          <time>          <version>          <desc>
 * Team              2024-11-20      1.0                Initial implementation
 */
@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Function: page
     * Description: Paginates through the list of books with optional filters.
     * Calls: BookService.page, BookService.count
     * Input: bookName (String) - Filter by book name
     *        minPrice (Double) - Filter by minimum price
     *        maxPrice (Double) - Filter by maximum price
     *        conditionLevel (String) - Filter by book condition
     *        pageNum (int) - Page number (default 1)
     *        pageSize (int) - Page size (default 10)
     *        sortBy (String) - Sort field
     * Output: PageResponse<Book> - Paginated list of books
     * Return: ResponseEntity<?>
     */
    @GetMapping("/page")
    public ResponseEntity<?> page(@RequestParam(value = "bookName", required = false) String bookName,
                                  @RequestParam(value = "minPrice", required = false) Double minPrice,
                                  @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                  @RequestParam(value = "conditionLevel", required = false) String conditionLevel,
                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "sortBy", required = false) String sortBy) {
        List<Book> records = bookService.page(bookName, minPrice, maxPrice, conditionLevel, pageNum, pageSize, sortBy);
        long total = bookService.count(bookName, minPrice, maxPrice);
        return ResponseEntity.ok(new PageResponse<>(total, records));
    }

    /**
     * Function: detail
     * Description: Retrieves the details of a specific book by ID.
     * Calls: BookService.get
     * Input: id (Long) - Book ID
     * Output: Book - Book details
     * Return: ResponseEntity<?>
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable("id") Long id) {
        Book b = bookService.get(id);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(b);
    }

    /**
     * Function: add
     * Description: Adds a new book to the system.
     * Calls: UserService.getByToken, BookService.add
     * Input: token (String) - User token
     *        book (Book) - Book object to add
     * Output: Book - Created book object
     * Return: ResponseEntity<?>
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Book book) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Book created = bookService.add(book, user.getUsername());
        return ResponseEntity.ok(created);
    }

    /**
     * Function: ownerList
     * Description: Lists all books uploaded by the current user (seller).
     * Calls: UserService.getByToken, BookService.listBySeller
     * Input: token (String) - User token
     * Output: List<Book> - List of books owned by the user
     * Return: ResponseEntity<?>
     */
    @GetMapping("/owner/list")
    public ResponseEntity<?> ownerList(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Book> list = bookService.listBySeller(user.getUsername());
        return ResponseEntity.ok(list);
    }

    /**
     * Function: update
     * Description: Updates the information of an existing book.
     * Calls: UserService.getByToken, BookService.update
     * Input: token (String) - User token
     *        id (Long) - Book ID
     *        book (Book) - Updated book object
     * Output: Book - Updated book object
     * Return: ResponseEntity<?>
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id,
                                    @RequestBody Book book) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        book.setId(id);
        Book updated = bookService.update(book, user.getUsername());
        if (updated == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(updated);
    }

    /**
     * Function: offline
     * Description: Takes a book offline (unshelve).
     * Calls: UserService.getByToken, BookService.offline
     * Input: token (String) - User token
     *        id (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     */
    @PostMapping("/offline/{id}")
    public ResponseEntity<?> offline(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.offline(id, user.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: delete
     * Description: Deletes a book from the system.
     * Calls: UserService.getByToken, BookService.delete
     * Input: token (String) - User token
     *        id (Long) - Book ID
     * Output: None
     * Return: ResponseEntity<?>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.delete(id, user.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok().build();
    }

    /**
     * Function: hot
     * Description: Retrieves a list of hot/popular books.
     * Calls: BookService.getHotBooks
     * Input: limit (int) - Number of books to return (default 6)
     * Output: List<Book> - List of hot books
     * Return: ResponseEntity<?>
     */
    @GetMapping("/hot")
    public ResponseEntity<?> hot(@RequestParam(value = "limit", defaultValue = "6") int limit) {
        return ResponseEntity.ok(bookService.getHotBooks(limit));
    }

    /**
     * Function: suggestions
     * Description: Provides search suggestions based on a keyword.
     * Calls: BookService.getSuggestions
     * Input: keyword (String) - Search keyword
     * Output: List<String> - List of suggestions
     * Return: ResponseEntity<?>
     */
    @GetMapping("/suggestions")
    public ResponseEntity<?> suggestions(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(bookService.getSuggestions(keyword));
    }
}
