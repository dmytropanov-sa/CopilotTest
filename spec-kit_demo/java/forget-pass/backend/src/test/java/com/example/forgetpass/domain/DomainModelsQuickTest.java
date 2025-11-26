package com.example.forgetpass.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainModelsQuickTest {

    @Test
    void patient_getters_setters_and_lifecycle() {
        Patient p = new Patient();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setEmail("john@example.com");
        p.setPhoneNumber("123");
        p.setAccountStatus("active");

        assertThat(p.getFirstName()).isEqualTo("John");
        assertThat(p.getLastName()).isEqualTo("Doe");
        assertThat(p.getEmail()).isEqualTo("john@example.com");
        assertThat(p.getPhoneNumber()).isEqualTo("123");
        assertThat(p.getAccountStatus()).isEqualTo("active");

        // lifecycle hooks
        p.setCreatedAt(Instant.EPOCH);
        p.onCreate();
        assertThat(p.getPatientId()).isNotNull();
        assertThat(p.getCreatedAt()).isAfter(Instant.EPOCH);

        p.setUpdatedAt(Instant.EPOCH);
        p.onUpdate();
        assertThat(p.getUpdatedAt()).isAfter(Instant.EPOCH);
    }

    @Test
    void patientCredential_previousHashes_and_pushBehavior() {
        PatientCredential pc = new PatientCredential();

        // initially empty
        pc.setPreviousPasswordHashesJson(null);
        assertThat(pc.getPreviousPasswordHashes()).isEmpty();

        // set explicit history
        pc.setPreviousPasswordHashes(List.of("old1", "old2"));
        List<String> before = pc.getPreviousPasswordHashes();
        assertThat(before).containsExactly("old1", "old2");

        // push a new hash with maxHistory=2 -> keep most-recent two
        pc.pushPreviousPasswordHash("new", 2);
        List<String> after = pc.getPreviousPasswordHashes();
        assertThat(after).containsExactly("new", "old1");

        // pushing blank/null should be ignored
        pc.pushPreviousPasswordHash("", 3);
        assertThat(pc.getPreviousPasswordHashes()).containsExactly("new", "old1");
    }

    @Test
    void authenticationAuditLog_onCreate_and_fields() {
        AuthenticationAuditLog a = new AuthenticationAuditLog();
        a.setEventType("LOGIN");
        a.setIpAddress("1.2.3.4");
        a.setUserAgent("ua");
        a.setSuccess(true);

        a.setLogId(null);
        a.setTimestamp(Instant.EPOCH);
        a.onCreate();

        assertThat(a.getLogId()).isNotNull();
        assertThat(a.getTimestamp()).isAfter(Instant.EPOCH);
        assertThat(a.getEventType()).isEqualTo("LOGIN");
        assertThat(a.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(a.getUserAgent()).isEqualTo("ua");
        assertThat(a.isSuccess()).isTrue();
    }

    @Test
    void tokens_onCreate_setters() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setTokenId(null);
        prt.setCreatedAt(Instant.EPOCH);
        prt.onCreate();
        assertThat(prt.getTokenId()).isNotNull();
        assertThat(prt.getCreatedAt()).isAfter(Instant.EPOCH);

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setTokenId(null);
        evt.setCreatedAt(Instant.EPOCH);
        evt.onCreate();
        assertThat(evt.getTokenId()).isNotNull();
        assertThat(evt.getCreatedAt()).isAfter(Instant.EPOCH);
    }

    @Test
    void patientCredential_gettersSetters_and_lifecycle() {
        PatientCredential pc = new PatientCredential();
        UUID id = UUID.randomUUID();
        pc.setCredentialId(id);
        assertThat(pc.getCredentialId()).isEqualTo(id);

        Patient p = new Patient();
        pc.setPatient(p);
        assertThat(pc.getPatient()).isSameAs(p);

        pc.setPasswordHash("hash");
        assertThat(pc.getPasswordHash()).isEqualTo("hash");

        Instant now = Instant.now();
        pc.setPasswordChangedAt(now);
        assertThat(pc.getPasswordChangedAt()).isEqualTo(now);

        pc.setFailedLoginAttempts(2);
        assertThat(pc.getFailedLoginAttempts()).isEqualTo(2);

        pc.setLockedUntil(now);
        assertThat(pc.getLockedUntil()).isEqualTo(now);

        pc.setCreatedAt(now);
        assertThat(pc.getCreatedAt()).isEqualTo(now);

        pc.setUpdatedAt(now);
        assertThat(pc.getUpdatedAt()).isEqualTo(now);

        pc.setPreviousPasswordHashesJson("[\"a\"]");
        assertThat(pc.getPreviousPasswordHashesJson()).isEqualTo("[\"a\"]");

        pc.setPreviousPasswordHashes(java.util.List.of("a","b"));
        assertThat(pc.getPreviousPasswordHashes()).containsExactly("a","b");

        pc.pushPreviousPasswordHash("c", 3);
        assertThat(pc.getPreviousPasswordHashes()).contains("c");

        // lifecycle callbacks
        pc.onCreate();
        pc.onUpdate();

    }

    @Test
    void patient_and_tokens_getters_and_lifecycle() {
        Patient patient = new Patient();
        UUID pid = UUID.randomUUID();
        patient.setPatientId(pid);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("x@example.com");
        patient.setPhoneNumber("+1");
        patient.setDateOfBirth(LocalDate.of(1990,1,1));
        patient.setAccountStatus("ACTIVE");
        patient.setCreatedAt(Instant.now());
        patient.setUpdatedAt(Instant.now());
        patient.setLastLoginAt(Instant.now());

        assertThat(patient.getPatientId()).isEqualTo(pid);
        assertThat(patient.getFirstName()).isEqualTo("John");

        patient.onCreate();
        patient.onUpdate();

        PasswordResetToken prt = new PasswordResetToken();
        prt.onCreate();
        prt.setTokenId(UUID.randomUUID());
        prt.setIpAddress("1.2.3.4");
        prt.setUserAgent("ua");

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.onCreate();
        evt.setTokenId(UUID.randomUUID());
        evt.setIpAddress("1.2.3.4");
        evt.setUserAgent("ua");

        AuthenticationAuditLog aal = new AuthenticationAuditLog();
        aal.onCreate();
        aal.setIpAddress("1.2.3.4");
        aal.setUserAgent("ua");

        // simple assertions to touch getters
        assertThat(prt.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(evt.getUserAgent()).isEqualTo("ua");
        assertThat(aal.getIpAddress()).isEqualTo("1.2.3.4");
    }
}
