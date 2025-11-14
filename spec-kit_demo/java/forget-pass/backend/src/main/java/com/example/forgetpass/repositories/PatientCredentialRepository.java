package com.example.forgetpass.repositories;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientCredentialRepository extends JpaRepository<PatientCredential, UUID> {
    Optional<PatientCredential> findByPatient(Patient patient);
}
