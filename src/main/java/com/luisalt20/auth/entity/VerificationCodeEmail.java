package com.luisalt20.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "verification_code_email")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationCodeEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
