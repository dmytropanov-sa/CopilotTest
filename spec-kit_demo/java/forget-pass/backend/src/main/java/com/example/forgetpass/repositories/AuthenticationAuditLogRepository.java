package com.example.forgetpass.repositories;

import com.example.forgetpass.domain.AuthenticationAuditLog;
import com.example.forgetpass.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthenticationAuditLogRepository extends JpaRepository<AuthenticationAuditLog, UUID> {
    List<AuthenticationAuditLog> findByPatient(Patient patient);
}
