package com.devlib.auth.config;

import com.devlib.auth.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Factory class that builds the default SecurityFilterChain.
 *
 * Intentionally NOT a @Configuration bean — it is instantiated
 * and called from JwtAuthAutoConfiguration which IS a Spring bean.
 *
 * Default policy:
 *   - CSRF disabled (stateless REST API)
 *   - Sessions: STATELESS
 *   - Public endpoints: configurable via auth.jwt.public-endpoints
 *   - Everything else: requires authentication
 *   - Clean JSON 401 / 403 responses
 */
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtProperties jwtProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtProperties jwtProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtProperties = jwtProperties;
    }

    public SecurityFilterChain build(HttpSecurity http) throws Exception {
        log.info("[JWT Starter] Configuring SecurityFilterChain. Public: {}",
            String.join(", ", jwtProperties.getPublicEndpoints()));

        http
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)

            // No HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(jwtProperties.getPublicEndpoints()).permitAll()
                .anyRequest().authenticated()
            )

            // Custom JSON error responses
            .exceptionHandling(ex -> ex

                // 401 — token missing or invalid
                .authenticationEntryPoint((request, response, e) -> {
                    log.debug("[JWT Starter] 401 Unauthorized → {}", request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(String.format(
                        "{\"status\":401,\"error\":\"Unauthorized\"," +
                        "\"message\":\"Authentication token is missing or invalid.\"," +
                        "\"path\":\"%s\"}", request.getRequestURI()
                    ));
                })

                // 403 — authenticated but insufficient role
                .accessDeniedHandler((request, response, e) -> {
                    log.debug("[JWT Starter] 403 Forbidden → {}", request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(String.format(
                        "{\"status\":403,\"error\":\"Forbidden\"," +
                        "\"message\":\"You do not have permission to access this resource.\"," +
                        "\"path\":\"%s\"}", request.getRequestURI()
                    ));
                })
            )

            // Run JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
