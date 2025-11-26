package com.example.forgetpass.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainModelsCoverageTest {

    @Test
    void patientLifecycleAndAccessors() {
        Patient p = new Patient();
        UUID id = UUID.randomUUID();
        p.setPatientId(id);
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setEmail("john@example.com");
        p.setPhoneNumber("123");
        p.setDateOfBirth(LocalDate.of(1990,1,1));
        p.setAccountStatus("ACTIVE");
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p.setLastLoginAt(Instant.now());

        assertThat(p.getPatientId()).isEqualTo(id);
        assertThat(p.getFirstName()).isEqualTo("John");
        assertThat(p.getLastName()).isEqualTo("Doe");
        assertThat(p.getEmail()).isEqualTo("john@example.com");
        assertThat(p.getPhoneNumber()).isEqualTo("123");
        assertThat(p.getDateOfBirth()).isEqualTo(LocalDate.of(1990,1,1));
        assertThat(p.getAccountStatus()).isEqualTo("ACTIVE");

        // lifecycle
        p.onCreate();
        p.onUpdate();
    }

    @Test
    void patientCredentialAccessorsAndHelpers() {
        PatientCredential c = new PatientCredential();
        UUID credId = UUID.randomUUID();
        c.setCredentialId(credId);
        Patient p = new Patient();
        c.setPatient(p);
        c.setPasswordHash("hash");
        c.setPasswordChangedAt(Instant.now());
        c.setFailedLoginAttempts(2);
        c.setLockedUntil(Instant.now());
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());

        assertThat(c.getCredentialId()).isEqualTo(credId);
        assertThat(c.getPatient()).isSameAs(p);
        assertThat(c.getPasswordHash()).isEqualTo("hash");
        assertThat(c.getFailedLoginAttempts()).isEqualTo(2);

        List<String> prev = new ArrayList<>();
        prev.add("a");
        c.setPreviousPasswordHashes(prev);
        assertThat(c.getPreviousPasswordHashes()).containsExactly("a");

        c.pushPreviousPasswordHash("b", 5);
        assertThat(c.getPreviousPasswordHashes()).contains("b");

        c.onCreate();
        c.onUpdate();
    }

    @Test
    void authAuditLogAccessors() {
        AuthenticationAuditLog l = new AuthenticationAuditLog();
        UUID id = UUID.randomUUID();
        l.setLogId(id);
        Patient p = new Patient();
        l.setPatient(p);
        l.setEventType("LOGIN");
        l.setIpAddress("1.2.3.4");
        l.setUserAgent("ua");
        l.setTimestamp(Instant.now());
        l.setSuccess(true);
        l.setMetadataJson("{}");

        assertThat(l.getLogId()).isEqualTo(id);
        assertThat(l.getPatient()).isSameAs(p);
        assertThat(l.getEventType()).isEqualTo("LOGIN");
        assertThat(l.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(l.getUserAgent()).isEqualTo("ua");
        assertThat(l.isSuccess()).isTrue();

        l.onCreate();
    }

    @Test
    void tokensAccessorsAndLifecycle() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setTokenId(UUID.randomUUID());
        prt.setTokenHash("t");
        prt.setExpiresAt(Instant.now());
        prt.setCreatedAt(Instant.now());
        prt.setUsedAt(Instant.now());
        prt.setIpAddress("ip");
        prt.setUserAgent("ua");

        prt.onCreate();

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setTokenId(UUID.randomUUID());
        evt.setTokenHash("h");
        evt.setExpiresAt(Instant.now());
        evt.setCreatedAt(Instant.now());
        evt.setVerifiedAt(Instant.now());
        evt.setResendCount(1);
        evt.onCreate();
    }
}
