package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity for application users.
 * The starter library has zero knowledge of this class —
 * it is entirely owned by the consumer application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /**
     * Role stored as string: "ROLE_USER" or "ROLE_ADMIN"
     * Must match Spring Security's GrantedAuthority format.
     */
    @Column(nullable = false)
    private String role;
}
