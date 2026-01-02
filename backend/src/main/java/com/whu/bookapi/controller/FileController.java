package com.whu.bookapi.controller;

import com.whu.bookapi.model.User;
import com.whu.bookapi.service.FileStorageService;
import com.whu.bookapi.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: FileController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for file upload and retrieval (e.g., book covers).
 * Others:
 * Function List:
 * 1. upload - Upload a file
 * 2. meta - Get file metadata
 * 3. raw - Get raw file content
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
@RestController
@RequestMapping("/files")
public class FileController {
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public FileController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    /**
     * Function: upload
     * Description: Uploads a file (image) to the server.
     * Calls: UserService.getByToken, FileStorageService.save
     * Called By: Frontend Book Publish / User Profile
     * Table Accessed: user_token, users, files (if DB used)
     * Table Updated: files (if DB used)
     * Input: token (String) - User token
     *        file (MultipartFile) - File to upload
     * Output: Map - Uploaded file details (id, url)
     * Return: ResponseEntity<?>
     * Others: Generates a raw URL for accessing the file.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestHeader(value = "token", required = false) String token,
                                    @RequestPart("file") MultipartFile file) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long id;
        try {
            id = fileStorageService.save(file, u.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(Long.toString(id))
                .path("/raw")
                .build()
                .toUri()
                .getPath();
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("url", url);
        resp.put("originalName", file.getOriginalFilename());
        return ResponseEntity.ok(resp);
    }

    /**
     * Function: meta
     * Description: Retrieves metadata for a file.
     * Calls: FileStorageService.getMeta
     * Called By: Frontend
     * Table Accessed: files (if DB used)
     * Table Updated: None
     * Input: id (long) - File ID
     * Output: Map - File metadata
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/{id}/meta")
    public ResponseEntity<?> meta(@PathVariable("id") long id) {
        Map<String, Object> meta = fileStorageService.getMeta(id);
        if (meta == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(meta);
    }

    /**
     * Function: raw
     * Description: Retrieves the raw content of a file (downloads/views it).
     * Calls: FileStorageService.getLocation
     * Called By: Browser / Frontend Image Tags
     * Table Accessed: files (if DB used)
     * Table Updated: None
     * Input: id (long) - File ID
     * Output: Resource - File content
     * Return: ResponseEntity<?>
     * Others: Handles Content-Type and Content-Disposition.
     */
    @GetMapping("/{id}/raw")
    public ResponseEntity<?> raw(@PathVariable("id") long id) {
        FileStorageService.StoredFileLocation loc = fileStorageService.getLocation(id);
        if (loc == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        try {
            Path p = Path.of(loc.storagePath()).toAbsolutePath().normalize();
            Resource r = new UrlResource(p.toUri());
            if (!r.exists() || !r.isReadable()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
            if (loc.contentType() != null && !loc.contentType().isBlank()) {
                mt = MediaType.parseMediaType(loc.contentType());
            }
            return ResponseEntity.ok()
                    .contentType(mt)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(loc.originalName()) + "\"")
                    .body(r);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static String safeFilename(String name) {
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[\\r\\n\"]", "_");
    }
}
