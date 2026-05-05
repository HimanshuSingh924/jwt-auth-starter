package com.devlib.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Fallback UserDetailsService — registered ONLY when the consumer app
 * does NOT declare their own UserDetailsService bean.
 *
 * This always throws UsernameNotFoundException with a helpful message
 * guiding the developer to provide their own implementation.
 *
 * HOW TO OVERRIDE:
 *   Simply declare a @Service class implementing UserDetailsService in
 *   your application. The @ConditionalOnMissingBean in JwtAuthAutoConfiguration
 *   will detect it and skip this default entirely.
 */
public class DefaultUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.error(
            "[JWT Starter] ⚠ DefaultUserDetailsService called for '{}'. " +
            "You MUST provide your own @Service implementing UserDetailsService " +
            "that loads users from your database.", username
        );
        throw new UsernameNotFoundException(
            "No UserDetailsService implementation found in your application. " +
            "Please create a @Service class that implements UserDetailsService."
        );
    }
}
