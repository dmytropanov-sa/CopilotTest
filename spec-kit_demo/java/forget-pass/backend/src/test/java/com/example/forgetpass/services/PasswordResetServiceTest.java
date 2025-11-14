package com.example.forgetpass.services;

import com.example.forgetpass.domain.PasswordResetToken;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import com.example.forgetpass.repositories.PasswordResetTokenRepository;
import com.example.forgetpass.repositories.PatientCredentialRepository;
import com.example.forgetpass.repositories.PatientRepository;
import com.example.forgetpass.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock PatientRepository patientRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PatientCredentialRepository credentialRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock AuditService auditService;
    @Mock PasswordValidationService passwordValidationService;

    @InjectMocks PasswordResetService service;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setEmail("jane.doe@example.com");
    }

    @Test
    void requestReset_invalidatesPreviousAndSendsEmail_whenPatientExists() {
        when(patientRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(patient));
        List<PasswordResetToken> active = new ArrayList<>();
        PasswordResetToken old = new PasswordResetToken();
        old.setPatient(patient);
        active.add(old);
        when(tokenRepository.findByPatientAndUsedAtIsNull(patient)).thenReturn(active);

        ArgumentCaptor<PasswordResetToken> prtCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        service.requestReset("jane.doe@example.com", "https://app.local");

        // previous tokens invalidated
        assertThat(old.getUsedAt()).isNotNull();
        verify(tokenRepository).saveAll(active);

        // new token saved with 1h expiry
        verify(tokenRepository).save(prtCaptor.capture());
        PasswordResetToken saved = prtCaptor.getValue();
        assertThat(saved.getPatient()).isEqualTo(patient);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        assertThat(Duration.between(Instant.now(), saved.getExpiresAt()).toMinutes()).isBetween(59L, 61L);

        // email dispatched
        verify(emailService, times(1)).sendPasswordResetEmail(eq("jane.doe@example.com"), anyString());
        verify(auditService, atLeastOnce()).log(eq("password_reset_request"), eq(patient), any(), any(), eq(true), any());
    }

    @Test
    void requestReset_succeeds_silently_whenPatientNotFound() {
        when(patientRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        service.requestReset("missing@example.com", "https://app.local");
        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(emailService);
        verify(auditService, atLeastOnce()).log(eq("password_reset_request"), isNull(), any(), any(), eq(true), any());
    }

    @Test
    void validateToken_returnsTrue_forActiveUnexpiredToken() {
        String token = "T0k3n-ABC";
        String hash = TokenUtil.sha256(token);
        PasswordResetToken t = new PasswordResetToken();
        t.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)));
        t.setUsedAt(null);
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(t));

        assertThat(service.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forExpiredOrUsedOrMissing() {
        String token = "expired";
        String hash = TokenUtil.sha256(token);

        PasswordResetToken expired = new PasswordResetToken();
        expired.setExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));
        expired.setUsedAt(null);
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expired));
        assertThat(service.validateToken(token)).isFalse();

        PasswordResetToken used = new PasswordResetToken();
        used.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        used.setUsedAt(Instant.now());
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(used));
        assertThat(service.validateToken(token)).isFalse();

        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.empty());
        assertThat(service.validateToken(token)).isFalse();
    }

    @Test
    void confirm_throws_forWeakPassword() {
        when(passwordValidationService.meetsPolicy("weak")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.confirm("tok", "weak"));
        verify(auditService, atLeastOnce()).log(eq("password_reset_confirm"), isNull(), isNull(), isNull(), eq(false), any());
    }

    @Test
    void confirm_returnsFalse_whenTokenMissingOrExpiredOrUsedOrNoCredential() {
        String token = "tok-xyz";
        String hash = TokenUtil.sha256(token);

        when(passwordValidationService.meetsPolicy("StrongP@ssw0rd!"))
                .thenReturn(true);

        // missing token
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.empty());
        assertThat(service.confirm(token, "StrongP@ssw0rd!")).isFalse();
        verify(auditService, atLeastOnce()).log(eq("password_reset_confirm"), isNull(), isNull(), isNull(), eq(false), any());

        // expired token
        PasswordResetToken expired = new PasswordResetToken();
        expired.setExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));
        expired.setUsedAt(null);
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expired));
        assertThat(service.confirm(token, "StrongP@ssw0rd!")).isFalse();
        verify(auditService, atLeastOnce()).log(eq("password_reset_confirm"), any(), isNull(), isNull(), eq(false), any());

        // used token
        PasswordResetToken used = new PasswordResetToken();
        used.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        used.setUsedAt(Instant.now());
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(used));
        assertThat(service.confirm(token, "StrongP@ssw0rd!")).isFalse();
        verify(auditService, atLeastOnce()).log(eq("password_reset_confirm"), any(), isNull(), isNull(), eq(false), any());

        // valid token but no credential
        PasswordResetToken valid = new PasswordResetToken();
        valid.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        valid.setUsedAt(null);
        Patient p = new Patient();
        p.setEmail("jane.doe@example.com");
        valid.setPatient(p);
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(valid));
        when(credentialRepository.findByPatient(p)).thenReturn(Optional.empty());
        assertThat(service.confirm(token, "StrongP@ssw0rd!")).isFalse();
    }

    @Test
    void confirm_updatesCredential_invalidatesTokens_andSendsConfirmation() {
        String token = "tok-valid";
        String hash = TokenUtil.sha256(token);

        when(passwordValidationService.meetsPolicy("StrongP@ssw0rd!")).thenReturn(true);

        PasswordResetToken t = new PasswordResetToken();
        t.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        t.setUsedAt(null);
        Patient p = new Patient();
        p.setEmail("jane.doe@example.com");
        t.setPatient(p);
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(t));

        PatientCredential cred = new PatientCredential();
        cred.setPatient(p);
        when(credentialRepository.findByPatient(p)).thenReturn(Optional.of(cred));
        when(passwordEncoder.encode("StrongP@ssw0rd!")).thenReturn("ENCODED_HASH");

        List<PasswordResetToken> others = new ArrayList<>();
        PasswordResetToken o1 = new PasswordResetToken(); o1.setPatient(p);
        PasswordResetToken o2 = new PasswordResetToken(); o2.setPatient(p);
        others.add(o1); others.add(o2);
        when(tokenRepository.findByPatientAndUsedAtIsNull(p)).thenReturn(others);

        boolean ok = service.confirm(token, "StrongP@ssw0rd!");
        assertThat(ok).isTrue();

        // password updated
        assertThat(cred.getPasswordHash()).isEqualTo("ENCODED_HASH");
        verify(credentialRepository, times(1)).save(cred);

        // this token and others invalidated
        assertThat(t.getUsedAt()).isNotNull();
        assertThat(o1.getUsedAt()).isNotNull();
        assertThat(o2.getUsedAt()).isNotNull();
        verify(tokenRepository, times(1)).save(t);
        verify(tokenRepository, times(1)).saveAll(others);

        // confirmation email
        verify(emailService, times(1)).sendPasswordChangedConfirmation("jane.doe@example.com");
        verify(auditService, atLeastOnce()).log(eq("password_reset_confirm"), eq(p), isNull(), isNull(), eq(true), any());
    }
}
