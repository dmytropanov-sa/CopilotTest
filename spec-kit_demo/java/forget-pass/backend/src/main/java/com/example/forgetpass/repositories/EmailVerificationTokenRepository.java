package com.example.forgetpass.repositories;

import com.example.forgetpass.domain.EmailVerificationToken;
import com.example.forgetpass.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    List<EmailVerificationToken> findByPatient(Patient patient);
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    long countByPatientAndCreatedAtAfter(Patient patient, java.time.Instant after);
}
