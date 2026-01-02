package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: BookService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Service class for managing book listings.
 *              Handles book creation, retrieval, updates, deletions, and stock management.
 *              Includes features for pagination, filtering, search suggestions, and administrative approvals.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2024-11-20      1.0                Initial implementation.
 * WiseBookPal Team  2024-12-25      1.1                Added stock management and admin review workflow.
 */
@Service
public class BookService {
    private final JdbcTemplate jdbcTemplate;

    public BookService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Function: add
     * Description: Adds a new book listing to the database.
     *              Sets initial status to 'under_review' and default stock to 1.
     * Called By: BookController.add
     * Table Accessed: books
     * Table Updated: books
     * Input: book (Book) - The book object to add
     *        sellerName (String) - The name of the seller adding the book
     * Output: Book - The added book object with generated ID
     * Return: Book
     */
    public Book add(Book book, String sellerName) {
        if (book == null) return null;
        book.setSellerName(sellerName);
        if (book.getConditionLevel() == null) book.setConditionLevel("九成新");
        if (book.getStock() == null) book.setStock(1);
        book.setStatus("under_review");
        book.setCreatedAt(System.currentTimeMillis());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO books (book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, book.getBookName());
            ps.setString(2, book.getAuthor());
            ps.setObject(3, book.getOriginalPrice());
            ps.setObject(4, book.getSellPrice());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getSellerName());
            ps.setString(7, book.getCoverUrl());
            ps.setString(8, book.getIsbn());
            ps.setString(9, book.getPublisher());
            ps.setString(10, book.getPublishDate());
            ps.setString(11, book.getConditionLevel());
            ps.setInt(12, book.getStock() == null ? 1 : book.getStock());
            ps.setString(13, book.getStatus());
            ps.setLong(14, book.getCreatedAt() == null ? System.currentTimeMillis() : book.getCreatedAt());
            ps.setString(15, book.getSellerType());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) book.setId(key.longValue());
        return book;
    }

    /**
     * Function: get
     * Description: Retrieves a book by its ID.
     * Called By: BookController.get, OrderService.createOrder
     * Table Accessed: books
     * Input: id (Long) - The ID of the book to retrieve
     * Output: Book - The book object, or null if not found
     * Return: Book
     */
    public Book get(Long id) {
        if (id == null) return null;
        java.util.List<Book> list = jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE id = ?",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Function: tryReserveStock
     * Description: Attempts to reserve stock for a book order.
     *              Decrements stock by 1. If stock reaches 0, sets status to 'offline'.
     *              Only succeeds if stock is greater than 0.
     * Called By: OrderService.createOrder
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to reserve
     * Output: boolean - True if reservation successful, false otherwise
     * Return: boolean
     */
    public boolean tryReserveStock(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update(
                "UPDATE books " +
                        "SET stock = stock - 1, " +
                        "    status = CASE WHEN status = 'on_sale' AND stock - 1 <= 0 THEN 'offline' ELSE status END " +
                        "WHERE id = ? AND stock > 0",
                id
        );
        return updated > 0;
    }

    /**
     * Function: releaseStock
     * Description: Releases reserved stock (e.g., if an order is cancelled).
     *              Increments stock by 1. If status was 'offline' and stock becomes positive, sets status back to 'on_sale'.
     * Called By: OrderService.cancelOrder
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to release
     * Output: void
     * Return: void
     */
    public void releaseStock(Long id) {
        if (id == null) return;
        jdbcTemplate.update(
                "UPDATE books " +
                        "SET stock = stock + 1, " +
                        "    status = CASE WHEN status = 'offline' AND stock = 0 THEN 'on_sale' ELSE status END " +
                        "WHERE id = ?",
                id
        );
    }

    /**
     * Function: page
     * Description: Retrieves a paginated list of books based on various filters.
     *              Supports filtering by name, price range, and condition.
     *              Supports sorting by price or creation date.
     * Called By: BookController.page
     * Table Accessed: books
     * Input: bookName (String) - Search keyword for title, author, isbn, or description
     *        minPrice (Double) - Minimum price filter
     *        maxPrice (Double) - Maximum price filter
     *        conditionLevel (String) - Condition level filter
     *        pageNum (int) - Page number (1-based)
     *        pageSize (int) - Number of items per page
     *        sortBy (String) - Sort criteria ("price_asc", "price_desc", "created_desc")
     * Output: List<Book> - List of books matching criteria
     * Return: List<Book>
     */
    public List<Book> page(String bookName, Double minPrice, Double maxPrice, String conditionLevel, int pageNum, int pageSize, String sortBy) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 200);
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'on_sale' AND stock > 0");
        if (bookName != null && !bookName.isEmpty()) {
            // Enhanced search: Title, Author, ISBN, Description (Course Name)
            sb.append(" AND (LOWER(book_name) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(description) LIKE ?)");
            String like = "%" + bookName.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (minPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price <= ?");
            params.add(maxPrice);
        }
        if (conditionLevel != null && !conditionLevel.isEmpty()) {
            sb.append(" AND condition_level = ?");
            params.add(conditionLevel);
        }
        if ("price_asc".equals(sortBy)) sb.append(" ORDER BY sell_price ASC");
        else if ("price_desc".equals(sortBy)) sb.append(" ORDER BY sell_price DESC");
        else if ("created_desc".equals(sortBy)) sb.append(" ORDER BY created_at DESC");
        else sb.append(" ORDER BY id ASC");
        sb.append(" LIMIT ? OFFSET ?");
        params.add(safePageSize);
        params.add((safePageNum - 1) * safePageSize);
        return jdbcTemplate.query(
                sb.toString(),
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                params.toArray()
        );
    }

    /**
     * Function: getHotBooks
     * Description: Retrieves a list of random "hot" books.
     *              Currently implemented as random selection for demonstration.
     * Called By: BookController.listHot
     * Table Accessed: books
     * Input: limit (int) - Number of books to retrieve
     * Output: List<Book> - List of hot books
     * Return: List<Book>
     */
    public List<Book> getHotBooks(int limit) {
        // Simulate hot books by random selection for now
        String sql = "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'on_sale' AND stock > 0 ORDER BY RAND() LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Book b = new Book();
            b.setId(rs.getLong("id"));
            b.setBookName(rs.getString("book_name"));
            b.setAuthor(rs.getString("author"));
            b.setOriginalPrice((Double) rs.getObject("original_price"));
            b.setSellPrice((Double) rs.getObject("sell_price"));
            b.setDescription(rs.getString("description"));
            b.setSellerName(rs.getString("seller_name"));
            b.setCoverUrl(rs.getString("cover_url"));
            b.setIsbn(rs.getString("isbn"));
            b.setPublisher(rs.getString("publisher"));
            b.setPublishDate(rs.getString("publish_date"));
            b.setConditionLevel(rs.getString("condition_level"));
            b.setStock((Integer) rs.getObject("stock"));
            b.setStatus(rs.getString("status"));
            b.setCreatedAt(rs.getLong("created_at"));
            b.setSellerType(rs.getString("seller_type"));
            return b;
        }, limit);
    }

    /**
     * Function: getSuggestions
     * Description: Retrieves search suggestions based on a keyword.
     *              Returns distinct book names matching the keyword.
     * Called By: BookController.getSuggestions
     * Table Accessed: books
     * Input: keyword (String) - Search keyword
     * Output: List<String> - List of suggested book names
     * Return: List<String>
     */
    public List<String> getSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT book_name FROM books WHERE LOWER(book_name) LIKE ? AND status = 'on_sale' LIMIT 10";
        return jdbcTemplate.queryForList(sql, String.class, "%" + keyword.toLowerCase() + "%");
    }

    /**
     * Function: count
     * Description: Counts the total number of books matching search criteria.
     *              Used for pagination metadata.
     * Called By: BookController.page
     * Table Accessed: books
     * Input: bookName (String) - Search keyword
     *        minPrice (Double) - Minimum price filter
     *        maxPrice (Double) - Maximum price filter
     * Output: long - Total count of matching books
     * Return: long
     */
    public long count(String bookName, Double minPrice, Double maxPrice) {
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(1) FROM books WHERE status = 'on_sale' AND stock > 0");
        if (bookName != null && !bookName.isEmpty()) {
            sb.append(" AND (LOWER(book_name) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(description) LIKE ?)");
            String like = "%" + bookName.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (minPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price <= ?");
            params.add(maxPrice);
        }
        Long c = jdbcTemplate.queryForObject(sb.toString(), Long.class, params.toArray());
        return c == null ? 0 : c;
    }

    /**
     * Function: listHot
     * Description: Retrieves a list of the most recently added books.
     * Called By: BookController.listHot
     * Table Accessed: books
     * Input: limit (int) - Number of books to retrieve
     * Output: List<Book> - List of recent books
     * Return: List<Book>
     */
    public List<Book> listHot(int limit) {
        int n = Math.min(Math.max(limit, 1), 50);
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'on_sale' ORDER BY created_at DESC LIMIT ?",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                n
        );
    }

    /**
     * Function: update
     * Description: Updates an existing book listing.
     *              Resets status to 'under_review' after update.
     *              Verifies that the operator is the owner of the book.
     * Called By: BookController.update
     * Table Accessed: books
     * Table Updated: books
     * Input: incoming (Book) - The updated book data
     *        operator (String) - The username of the person performing the update
     * Output: Book - The updated book object, or null if update failed
     * Return: Book
     */
    public Book update(Book incoming, String operator) {
        if (incoming == null || incoming.getId() == null) return null;
        Book origin = get(incoming.getId());
        if (origin == null) return null;
        if (operator == null || !operator.equals(origin.getSellerName())) return null;
        if (incoming.getBookName() != null) origin.setBookName(incoming.getBookName());
        if (incoming.getAuthor() != null) origin.setAuthor(incoming.getAuthor());
        if (incoming.getOriginalPrice() != null) origin.setOriginalPrice(incoming.getOriginalPrice());
        if (incoming.getSellPrice() != null) origin.setSellPrice(incoming.getSellPrice());
        if (incoming.getDescription() != null) origin.setDescription(incoming.getDescription());
        if (incoming.getCoverUrl() != null) origin.setCoverUrl(incoming.getCoverUrl());
        if (incoming.getIsbn() != null) origin.setIsbn(incoming.getIsbn());
        if (incoming.getPublisher() != null) origin.setPublisher(incoming.getPublisher());
        if (incoming.getPublishDate() != null) origin.setPublishDate(incoming.getPublishDate());
        if (incoming.getConditionLevel() != null) origin.setConditionLevel(incoming.getConditionLevel());
        if (incoming.getStock() != null) origin.setStock(incoming.getStock());
        origin.setStatus("under_review");
        jdbcTemplate.update(
                "UPDATE books SET book_name=?, author=?, original_price=?, sell_price=?, description=?, cover_url=?, isbn=?, publisher=?, publish_date=?, condition_level=?, stock=?, status=?, seller_type=? WHERE id=? AND seller_name=?",
                origin.getBookName(),
                origin.getAuthor(),
                origin.getOriginalPrice(),
                origin.getSellPrice(),
                origin.getDescription(),
                origin.getCoverUrl(),
                origin.getIsbn(),
                origin.getPublisher(),
                origin.getPublishDate(),
                origin.getConditionLevel(),
                origin.getStock(),
                origin.getStatus(),
                origin.getSellerType(),
                origin.getId(),
                operator
        );
        return origin;
    }

    /**
     * Function: offline
     * Description: Takes a book listing offline (sets status to 'offline').
     *              Verifies that the operator is the owner of the book.
     * Called By: BookController.offline
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to take offline
     *        operator (String) - The username of the person performing the action
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean offline(Long id, String operator) {
        if (id == null || operator == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'offline' WHERE id = ? AND seller_name = ?", id, operator);
        return updated > 0;
    }

    /**
     * Function: delete
     * Description: Deletes a book listing from the database.
     *              Verifies that the operator is the owner of the book.
     * Called By: BookController.delete
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to delete
     *        operator (String) - The username of the person performing the action
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean delete(Long id, String operator) {
        if (id == null || operator == null) return false;
        int updated = jdbcTemplate.update("DELETE FROM books WHERE id = ? AND seller_name = ?", id, operator);
        return updated > 0;
    }

    /**
     * Function: listBySeller
     * Description: Retrieves all books listed by a specific seller.
     * Called By: BookController.listBySeller
     * Table Accessed: books
     * Input: sellerName (String) - The seller's username
     * Output: List<Book> - List of books by the seller
     * Return: List<Book>
     */
    public java.util.List<Book> listBySeller(String sellerName) {
        if (sellerName == null) return new java.util.ArrayList<>();
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE seller_name = ? ORDER BY id",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                sellerName
        );
    }

    /**
     * Function: listUnderReview
     * Description: Retrieves all books that are currently under review.
     *              Used by admins to moderate new listings.
     * Called By: AdminController.listPendingBooks
     * Table Accessed: books
     * Input: None
     * Output: List<Book> - List of books with 'under_review' status
     * Return: List<Book>
     */
    public List<Book> listUnderReview() {
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'under_review' ORDER BY id",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                }
        );
    }

    /**
     * Function: approve
     * Description: Approves a book listing, making it visible for sale.
     * Called By: AdminController.approveBook
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to approve
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean approve(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'on_sale' WHERE id = ?", id);
        return updated > 0;
    }

    /**
     * Function: reject
     * Description: Rejects a book listing.
     * Called By: AdminController.rejectBook
     * Table Accessed: books
     * Table Updated: books
     * Input: id (Long) - The ID of the book to reject
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean reject(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'rejected' WHERE id = ?", id);
        return updated > 0;
    }
}
