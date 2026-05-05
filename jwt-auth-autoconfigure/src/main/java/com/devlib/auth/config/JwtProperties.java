package com.devlib.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Binds all auth.jwt.* properties from application.properties / application.yml
 *
 * Example usage in consumer app:
 *   auth.jwt.secret=dGhpcy1pcy1teS1zZWNyZXQta2V5LWZvci1qd3Q=
 *   auth.jwt.expiration=86400000
 *   auth.jwt.public-endpoints=/api/auth/**,/actuator/health
 */
@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    /**
     * HMAC-SHA256 signing secret — must be Base64-encoded, min 32 characters.
     * OVERRIDE THIS in production via environment variable!
     */
    @NotBlank(message = "JWT secret must not be blank")
    @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
    private String secret = "dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWtleS1mb3ItZGV2bGliLXN0YXJ0ZXIh";

    /**
     * Token validity in milliseconds. Default = 86400000 (24 hours).
     */
    @Min(value = 60000, message = "Expiration must be at least 60000ms (1 minute)")
    private long expiration = 86_400_000L;

    /**
     * Prefix before the token in the Authorization header. Default = "Bearer"
     */
    @NotBlank
    private String tokenPrefix = "Bearer";

    /**
     * HTTP header name. Default = "Authorization"
     */
    @NotBlank
    private String headerName = "Authorization";

    /**
     * URL patterns that bypass JWT authentication (public endpoints).
     * Default = /api/auth/**, /actuator/health
     */
    private String[] publicEndpoints = {"/api/auth/**", "/actuator/health"};

    // ── Getters & Setters ─────────────────────────────────────────────────

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }

    public String getTokenPrefix() { return tokenPrefix; }
    public void setTokenPrefix(String tokenPrefix) { this.tokenPrefix = tokenPrefix; }

    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    public String[] getPublicEndpoints() { return publicEndpoints; }
    public void setPublicEndpoints(String[] publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }
}
