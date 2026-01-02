/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: BookApiApplication.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Main entry point for the WiseBookPal Backend API.
 *              Initializes Spring Boot application and enables scheduling.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class.
 */
@SpringBootApplication
@EnableScheduling
public class BookApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookApiApplication.class, args);
    }
}
