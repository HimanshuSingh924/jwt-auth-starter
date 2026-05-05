package com.devlib.auth.provider;

import com.devlib.auth.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core JWT utility — stateless, thread-safe singleton.
 *
 * Responsibilities:
 *   - Generate signed JWT from Authentication or UserDetails
 *   - Validate token (signature + expiry)
 *   - Extract claims (username, roles, expiry)
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String ROLES_CLAIM = "roles";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Decode Base64 secret and build HMAC-SHA key — done ONCE at startup
        this.signingKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtProperties.getSecret())
        );
        log.info("[JWT Starter] JwtTokenProvider initialized. Token expiry: {}ms",
            jwtProperties.getExpiration());
    }

    // ── Token Generation ─────────────────────────────────────────────────

    /**
     * Generate JWT from Spring Security Authentication object.
     */
    public String generateToken(Authentication authentication) {
        List<String> roles = extractRolesFromAuthorities(authentication.getAuthorities());
        return buildToken(authentication.getName(), roles);
    }

    /**
     * Generate JWT directly from UserDetails (useful after registration).
     */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = extractRolesFromAuthorities(userDetails.getAuthorities());
        return buildToken(userDetails.getUsername(), roles);
    }

    private String buildToken(String subject, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(subject)
                .claim(ROLES_CLAIM, roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ── Token Validation ─────────────────────────────────────────────────

    /**
     * Validates token for signature integrity and expiration.
     * Returns false instead of throwing — safe to use in filter without try-catch.
     */
    public boolean validateToken(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("[JWT Starter] Token expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("[JWT Starter] Unsupported JWT: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("[JWT Starter] Malformed JWT: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("[JWT Starter] Invalid signature: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("[JWT Starter] Empty/null token: {}", ex.getMessage());
        }
        return false;
    }

    // ── Claims Extraction ─────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts roles from JWT claim — null-safe.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object rolesClaim = extractClaim(token, claims -> claims.get(ROLES_CLAIM));
        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        }
        return Collections.emptyList(); // null-safe fallback
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseAllClaims(token));
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private Claims parseAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> extractRolesFromAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    // ── Accessors for Filter ──────────────────────────────────────────────

    public String getTokenPrefix() { return jwtProperties.getTokenPrefix(); }
    public String getHeaderName() { return jwtProperties.getHeaderName(); }
}
