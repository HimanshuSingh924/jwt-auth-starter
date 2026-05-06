package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Protected resource endpoints.
 *
 * No security code needed here — JwtAuthenticationFilter has already
 * validated the token and populated SecurityContextHolder before
 * any request reaches this controller.
 *
 * @AuthenticationPrincipal injects the currently logged-in UserDetails.
 * @PreAuthorize enables method-level role checks (enabled by starter).
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ResourceController {

    /**
     * GET /api/public/hello
     * Public — no JWT required (in auth.jwt.public-endpoints list)
     */
    @GetMapping("/public/hello")
    public ResponseEntity<?> publicHello() {
        return ResponseEntity.ok(Map.of(
                "message", "Hello! This is a public endpoint — no JWT needed.",
                "status", "ok"
        ));
    }

    /**
     * GET /api/me
     * Protected — requires ANY valid JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(Map.of(
                "email", currentUser.getUsername(),
                "roles", currentUser.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList()),
                "message", "Token is valid! You are authenticated."
        ));
    }

    /**
     * GET /api/user/data
     * Requires ROLE_USER or ROLE_ADMIN
     */
    @GetMapping("/user/data")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> userData(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome, " + currentUser.getUsername() + "!",
                "data", "This is your user data."
        ));
    }

    /**
     * GET /api/admin/dashboard
     * Requires ROLE_ADMIN — returns 403 for ROLE_USER
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> adminDashboard(@AuthenticationPrincipal UserDetails currentUser) {
        log.info("Admin '{}' accessed dashboard.", currentUser.getUsername());
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to the Admin Dashboard!",
                "admin", currentUser.getUsername(),
                "info", "Only ROLE_ADMIN can see this."
        ));
    }
}
