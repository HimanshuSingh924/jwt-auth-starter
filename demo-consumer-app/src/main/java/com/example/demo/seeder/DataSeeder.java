package com.example.demo.seeder;

import com.example.demo.model.AppUser;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds test users into the H2 in-memory database on app startup.
 *
 * Test credentials:
 *   alice@example.com  / password123  → ROLE_USER
 *   admin@example.com  / admin123     → ROLE_ADMIN
 *
 * Uses @Transactional at the method level — proper Spring-managed
 * transaction (fixes the common @Transactional-on-main-class bug).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Skip if already seeded (idempotent)
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] Users already exist — skipping seed.");
            return;
        }

        userRepository.save(AppUser.builder()
                .email("alice@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role("ROLE_USER")
                .build());

        userRepository.save(AppUser.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build());

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  ✅ jwt-auth-starter Demo — Test Users Ready!");
        log.info("  👤 alice@example.com  / password123  → ROLE_USER");
        log.info("  👑 admin@example.com  / admin123     → ROLE_ADMIN");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
