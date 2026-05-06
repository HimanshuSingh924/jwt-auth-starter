package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo Consumer Application
 *
 * ─── SETUP STEPS ──────────────────────────────────────────
 * 1. Run "mvn clean install -DskipTests" inside jwt-auth-starter-parent/
 * 2. Then run this application via Eclipse or "mvn spring-boot:run"
 *
 * ─── TEST USERS (seeded on startup) ───────────────────────
 *   alice@example.com  / password123  → ROLE_USER
 *   admin@example.com  / admin123     → ROLE_ADMIN
 *
 * ─── ENDPOINTS ────────────────────────────────────────────
 *   POST /api/auth/login       → Get JWT token
 *   GET  /api/public/hello     → No token needed
 *   GET  /api/me               → Any valid token
 *   GET  /api/user/data        → ROLE_USER or ROLE_ADMIN
 *   GET  /api/admin/dashboard  → ROLE_ADMIN only
 *   GET  /h2-console           → H2 database browser
 *
 * ─── PORT ─────────────────────────────────────────────────
 *   http://localhost:8080
 */
@SpringBootApplication
public class DemoConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }
}
