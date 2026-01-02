/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: FileStorageService.java
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Service for managing file storage and retrieval.
 *              - Handles file upload, storage, and metadata retrieval.
 *              - Computes SHA256 hash for file integrity/deduplication (potential).
 *              - Manages file storage paths and directories.
 * Others:
 * Function List:
 * 1. save - Saves an uploaded file to disk and records metadata.
 * 2. getMeta - Retrieves file metadata by ID.
 * 3. getLocation - Retrieves physical file location by ID.
 * 4. ensureUploadDir - Ensures the upload directory exists.
 * 5. sha256Hex - Computes SHA256 hash of an input stream.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

package com.whu.bookapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Service class for File Storage management.
 */
@Service
public class FileStorageService {
    private final JdbcTemplate jdbcTemplate;
    private final Path uploadDir;

    public FileStorageService(JdbcTemplate jdbcTemplate, @Value("${app.files.upload-dir:./uploads}") String uploadDir) {
        this.jdbcTemplate = jdbcTemplate;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Function: save
     * Description: Saves an uploaded file to the local filesystem and records its metadata in the database.
     *              - Generates a random UUID filename.
     *              - Computes SHA256 hash.
     *              - Stores file content.
     *              - Inserts metadata into 'stored_file' table.
     * Calls: ensureUploadDir, sha256Hex, JdbcTemplate.update
     * Called By: FileController.upload
     * Table Accessed: stored_file
     * Table Updated: stored_file
     * Input: file (MultipartFile), uploader (String - username)
     * Output: long (file ID)
     * Return: long
     * Others: Throws RuntimeException if file is empty or storage fails.
     */
    public long save(MultipartFile file, String uploader) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        String originalName0 = file.getOriginalFilename();
        final String originalName = (originalName0 == null || originalName0.isBlank()) ? "file" : originalName0;

        String storedName = UUID.randomUUID().toString().replace("-", "");
        Path target = ensureUploadDir().resolve(storedName);

        String sha256;
        try (InputStream in = file.getInputStream()) {
            sha256 = sha256Hex(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash file", e);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }

        long now = System.currentTimeMillis();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO stored_file (original_name, content_type, size, sha256, storage_path, uploader, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, originalName);

            ps.setString(2, file.getContentType());
            ps.setLong(3, file.getSize());
            ps.setString(4, sha256);
            ps.setString(5, target.toString());
            ps.setString(6, uploader);
            ps.setLong(7, now);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new RuntimeException("Failed to generate file id");
        return key.longValue();
    }

    /**
     * Function: getMeta
     * Description: Retrieves metadata for a stored file by its ID.
     * Calls: JdbcTemplate.queryForMap
     * Called By: FileController.getMeta
     * Table Accessed: stored_file
     * Table Updated: None
     * Input: id (long)
     * Output: Map<String, Object>
     * Return: Map<String, Object>
     */
    public Map<String, Object> getMeta(long id) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT id, original_name AS originalName, content_type AS contentType, size, sha256, uploader, created_at AS createdAt FROM stored_file WHERE id = ?",
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Function: getLocation
     * Description: Retrieves the physical storage location and basic info of a file by its ID.
     *              Used for serving the file content.
     * Calls: JdbcTemplate.queryForObject
     * Called By: FileController.download
     * Table Accessed: stored_file
     * Table Updated: None
     * Input: id (long)
     * Output: StoredFileLocation
     * Return: StoredFileLocation
     */
    public StoredFileLocation getLocation(long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT storage_path, original_name, content_type FROM stored_file WHERE id = ?",
                    (rs, rowNum) -> new StoredFileLocation(rs.getString("storage_path"), rs.getString("original_name"), rs.getString("content_type")),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Function: ensureUploadDir
     * Description: Ensures that the upload directory exists; creates it if necessary.
     * Calls: Files.createDirectories
     * Called By: save
     * Input: None
     * Output: Path
     * Return: Path
     */
    private Path ensureUploadDir() {
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create upload dir", e);
        }
        return uploadDir;
    }

    /**
     * Function: sha256Hex
     * Description: Computes the SHA-256 hash of the input stream.
     * Calls: MessageDigest.getInstance, HexFormat.of
     * Called By: save
     * Input: in (InputStream)
     * Output: String (Hex representation of SHA-256)
     * Return: String
     */
    private static String sha256Hex(InputStream in) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            digest.update(buf, 0, n);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Record: StoredFileLocation
     * Description: Helper record to hold file location, original name, and content type.
     */
    public record StoredFileLocation(String storagePath, String originalName, String contentType) {
    }
}
