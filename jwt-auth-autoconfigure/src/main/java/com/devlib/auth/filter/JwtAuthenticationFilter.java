package com.devlib.auth.filter;

import com.devlib.auth.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Intercepts every HTTP request ONCE and:
 *   1. Extracts JWT from Authorization header
 *   2. Validates the token
 *   3. Loads UserDetails
 *   4. Populates SecurityContextHolder
 *
 * If token is missing/invalid — SecurityContext stays empty → 401 returned downstream.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractToken(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                String username = jwtTokenProvider.extractUsername(jwt);

                // Only process if username found AND not already authenticated
                if (username != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Use roles from JWT claim (avoids extra DB call)
                    List<SimpleGrantedAuthority> authorities = jwtTokenProvider
                            .extractRoles(jwt)
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, authorities);

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("[JWT Starter] Authenticated '{}' → [{} {}]",
                            username, request.getMethod(), request.getRequestURI());
                }
            }
        } catch (Exception ex) {
            // Do NOT re-throw — bad token = 401, not 500
            log.error("[JWT Starter] Authentication failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts raw JWT string from "Authorization: Bearer <token>" header.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(jwtTokenProvider.getHeaderName());
        String prefix = jwtTokenProvider.getTokenPrefix();

        if (StringUtils.hasText(header)
                && header.toLowerCase().startsWith(prefix.toLowerCase() + " ")) {
            return header.substring(prefix.length()).trim();
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }
}
