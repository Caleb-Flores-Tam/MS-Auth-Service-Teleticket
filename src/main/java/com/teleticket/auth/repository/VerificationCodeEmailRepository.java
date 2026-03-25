package com.luisalt20.auth.repository;

import com.luisalt20.auth.entity.VerificationCodeEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeEmailRepository extends JpaRepository<VerificationCodeEmail, Long> {
    Optional<VerificationCodeEmail> findVerificationCodeEmailByEmailEqualsIgnoreCase(String email);

    boolean existsByEmailAndCodeAndCreatedAtAfter(String email, String code, LocalDateTime after);
}
