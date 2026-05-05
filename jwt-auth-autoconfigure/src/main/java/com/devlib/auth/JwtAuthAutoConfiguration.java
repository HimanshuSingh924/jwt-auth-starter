package com.devlib.auth;

import com.devlib.auth.config.JwtProperties;
import com.devlib.auth.config.SecurityConfig;
import com.devlib.auth.filter.JwtAuthenticationFilter;
import com.devlib.auth.provider.JwtTokenProvider;
import com.devlib.auth.service.DefaultUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ═══════════════════════════════════════════════════════════
 *  JWT Auth Starter — Master Auto-Configuration
 * ═══════════════════════════════════════════════════════════
 *
 * Discovered by Spring Boot via:
 *   META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 * Activates only when:
 *   @ConditionalOnWebApplication  → Running in a servlet web app
 *   @ConditionalOnClass           → Spring Security is on the classpath
 *
 * Every bean uses @ConditionalOnMissingBean so consumer apps can
 * override any component by simply declaring their own bean.
 *
 * Bean order:
 *   JwtProperties → JwtTokenProvider → JwtAuthenticationFilter → SecurityFilterChain
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpSecurity.class)
@EnableConfigurationProperties(JwtProperties.class)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class JwtAuthAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthAutoConfiguration.class);

    // ── Tier 1: Fallback beans (only if consumer doesn't provide their own) ──

    /**
     * No-op UserDetailsService — replaced automatically when consumer
     * provides their own @Service implementing UserDetailsService.
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService defaultUserDetailsService() {
        log.warn("[JWT Starter] ⚠ No UserDetailsService found! " +
                 "Using DefaultUserDetailsService — token validation WILL fail. " +
                 "Provide your own @Service implementing UserDetailsService.");
        return new DefaultUserDetailsService();
    }

    /**
     * BCrypt password encoder (strength=12).
     * Override with your own PasswordEncoder bean if needed.
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        log.debug("[JWT Starter] Registering BCryptPasswordEncoder (strength=12)");
        return new BCryptPasswordEncoder(12);
    }

    // ── Tier 2: Core beans ────────────────────────────────────────────────

    /**
     * JWT creation, validation, and claims extraction utility.
     */
    @Bean
    @ConditionalOnMissingBean(JwtTokenProvider.class)
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        log.debug("[JWT Starter] Registering JwtTokenProvider");
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * HTTP request interceptor that validates JWT on every request.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationFilter.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService) {
        log.debug("[JWT Starter] Registering JwtAuthenticationFilter");
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    // ── Tier 3: Security filter chain ────────────────────────────────────

    /**
     * Default SecurityFilterChain.
     *
     * Override this by declaring your own SecurityFilterChain bean.
     * Example:
     *
     *   @Bean
     *   public SecurityFilterChain myChain(HttpSecurity http,
     *                                      JwtAuthenticationFilter jwtFilter) throws Exception {
     *       return http
     *           .csrf(AbstractHttpConfigurer::disable)
     *           .authorizeHttpRequests(auth -> auth
     *               .requestMatchers("/public/**").permitAll()
     *               .anyRequest().authenticated())
     *           .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
     *           .build();
     *   }
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtProperties jwtProperties) throws Exception {
        log.info("[JWT Starter] Registering default SecurityFilterChain");
        return new SecurityConfig(jwtAuthenticationFilter, jwtProperties).build(http);
    }
}
