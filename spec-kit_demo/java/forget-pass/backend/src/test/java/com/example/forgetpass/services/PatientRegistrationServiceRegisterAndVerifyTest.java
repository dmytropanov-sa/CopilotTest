package com.example.forgetpass.services;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import com.example.forgetpass.repositories.PatientCredentialRepository;
import com.example.forgetpass.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PatientRegistrationServiceRegisterAndVerifyTest {

    PatientRepository patientRepository;
    PatientCredentialRepository credentialRepository;
    EmailValidationService emailValidationService;
    PasswordValidationService passwordValidationService;
    EmailVerificationService emailVerificationService;
    AuditService auditService;
    PasswordEncoder passwordEncoder;
    PatientRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepository.class);
        credentialRepository = mock(PatientCredentialRepository.class);
        emailValidationService = mock(EmailValidationService.class);
        passwordValidationService = mock(PasswordValidationService.class);
        emailVerificationService = mock(EmailVerificationService.class);
        auditService = mock(AuditService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        when(emailValidationService.isValidFormat(anyString())).thenReturn(true);
        when(emailValidationService.isDisposable(anyString())).thenReturn(false);
        when(passwordValidationService.meetsPolicy(anyString())).thenReturn(true);

        registrationService = new PatientRegistrationService(
            patientRepository,
            credentialRepository,
            passwordEncoder,
            emailValidationService,
            passwordValidationService,
            emailVerificationService,
            auditService
        );
    }

    @Test
    void registerAndIssueVerification_callsIssueVerification_andLogsSuccess() {
        Patient saved = new Patient();
        saved.setPatientId(java.util.UUID.randomUUID());
        saved.setEmail("ok@example.com");

        when(patientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        // sanity-check mocks
        assertThat(emailValidationService.isValidFormat("ok@example.com")).isTrue();
        assertThat(passwordValidationService.meetsPolicy("Strong1!")).isTrue();

        Patient p = registrationService.registerAndIssueVerification("A","B","ok@example.com","+1", LocalDate.now().minusYears(30), "Strong1!", "http://localhost");

        assertThat(p).isNotNull();
        verify(emailVerificationService, times(1)).issueVerification(eq(saved), anyString());
        verify(auditService, times(1)).log(eq("registration"), eq(saved), any(), any(), eq(true), any());
    }

    @Test
    void registerAndIssueVerification_handlesEmailFailure_andLogsFailure() {
        Patient saved = new Patient();
        saved.setPatientId(java.util.UUID.randomUUID());
        saved.setEmail("fail@example.com");

        when(patientRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);
        doThrow(new RuntimeException("send_failed")).when(emailVerificationService).issueVerification(eq(saved), anyString());

        // sanity-check mocks
        assertThat(emailValidationService.isValidFormat("fail@example.com")).isTrue();
        assertThat(passwordValidationService.meetsPolicy("Strong1!")).isTrue();

        Patient p = registrationService.registerAndIssueVerification("A","B","fail@example.com","+1", LocalDate.now().minusYears(30), "Strong1!", "http://localhost");

        assertThat(p).isNotNull();
        verify(emailVerificationService, times(1)).issueVerification(eq(saved), anyString());
        verify(auditService, times(1)).log(eq("registration"), eq(saved), any(), any(), eq(false), any());
    }
}
