package com.example.forgetpass.repositories;

import com.example.forgetpass.domain.PasswordResetToken;
import com.example.forgetpass.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    List<PasswordResetToken> findByPatient(Patient patient);
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    List<PasswordResetToken> findByPatientAndUsedAtIsNull(Patient patient);
    long countByPatientAndCreatedAtAfter(Patient patient, Instant after);
}
