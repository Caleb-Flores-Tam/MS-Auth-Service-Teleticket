package com.luisalt20.auth.entity;

import com.luisalt20.auth.dto.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = @Index(name = "uk_users_email", columnList = "email", unique = true))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "secret", unique = true, columnDefinition = "TEXT")
    private String secretBase32;

    @Column(nullable = false, name = "qr_code", unique = true, columnDefinition = "TEXT")
    private String qrCodeBase64;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}