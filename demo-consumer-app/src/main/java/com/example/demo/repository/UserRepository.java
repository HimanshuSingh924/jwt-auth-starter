package com.example.demo.repository;

import com.example.demo.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for AppUser — Spring Data auto-implements this.
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Find user by email — used by UserDetailsService for login.
     */
    Optional<AppUser> findByEmail(String email);
}
