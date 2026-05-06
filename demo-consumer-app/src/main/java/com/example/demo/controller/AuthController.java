package com.example.demo.controller;

import com.devlib.auth.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public authentication endpoint — no JWT required to access these.
 * (Configured via auth.jwt.public-endpoints=/api/auth/**)
 *
 * Notice: JwtTokenProvider and PasswordEncoder are injected directly
 * from the application context — the starter registered them automatically.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // These beans are auto-registered by the jwt-auth-starter
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     *
     * Request body:  { "email": "alice@example.com", "password": "password123" }
     * Response:      { "token": "eyJhbGci...", "type": "Bearer", "email": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Step 1: Load user from DB
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(request.email());

            // Step 2: Validate password
            if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
                log.warn("Failed login attempt for email: {}", request.email());
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid email or password"));
            }

            // Step 3: Build Authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Step 4: Generate JWT using starter's JwtTokenProvider
            String token = jwtTokenProvider.generateToken(authentication);

            log.info("User '{}' logged in successfully.", request.email());

            // Step 5: Return token to client
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "type", jwtTokenProvider.getTokenPrefix(),
                    "email", userDetails.getUsername(),
                    "role", userDetails.getAuthorities().iterator().next().getAuthority()
            ));

        } catch (UsernameNotFoundException ex) {
            log.warn("Login failed — user not found: {}", request.email());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception ex) {
            log.error("Unexpected login error: {}", ex.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // ── Request DTO ───────────────────────────────────────────────────────

    public record LoginRequest(String email, String password) {}
}
