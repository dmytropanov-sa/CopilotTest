package com.example.forgetpass.controllers;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.services.AuditService;
import com.example.forgetpass.services.PatientRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PatientRegistrationControllerTest {

    @Test
    void register_success_issuesVerification_andLogsAudit() {
        PatientRegistrationService registrationService = mock(PatientRegistrationService.class);
        AuditService auditService = mock(AuditService.class);
        com.example.forgetpass.services.ReCaptchaService reCaptchaService = mock(com.example.forgetpass.services.ReCaptchaService.class);
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);
        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);

        Patient p = new Patient();
                p.setPatientId(java.util.UUID.randomUUID());
        p.setEmail("new@example.com");
        when(registrationService.registerAndIssueVerification(eq("New"), eq("User"), eq("new@example.com"), eq("+1 555-1111"), any(LocalDate.class), eq("Str0ngP@ss!"), anyString()))
                .thenReturn(p);

        PatientRegistrationController.RegisterRequest req = new PatientRegistrationController.RegisterRequest(
                "New", "User", "new@example.com", "+1 555-1111", LocalDate.now().minusYears(30), "Str0ngP@ss!"
        );
        ResponseEntity<?> response = controller.register(req, "203.0.113.10", "JUnit-Agent");

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertThat(body.get("email")).isEqualTo("new@example.com");

                verify(registrationService, times(1)).registerAndIssueVerification(eq("New"), eq("User"), eq("new@example.com"), eq("+1 555-1111"), any(LocalDate.class), eq("Str0ngP@ss!"), anyString());
        verify(auditService, atLeastOnce()).log(eq("registration"), eq(p), eq("203.0.113.10"), eq("JUnit-Agent"), eq(true), any());
    }

    @Test
    void register_duplicateEmail_returnsConflict_andLogsAudit() {
        PatientRegistrationService registrationService = mock(PatientRegistrationService.class);
        AuditService auditService = mock(AuditService.class);
        com.example.forgetpass.services.ReCaptchaService reCaptchaService = mock(com.example.forgetpass.services.ReCaptchaService.class);
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);
        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);

        when(registrationService.registerAndIssueVerification(anyString(), anyString(), eq("exists@example.com"), anyString(), any(LocalDate.class), anyString(), anyString()))
                .thenThrow(new IllegalStateException("email_already_exists"));

        PatientRegistrationController.RegisterRequest req = new PatientRegistrationController.RegisterRequest(
                "New", "User", "exists@example.com", "+1 555-1111", LocalDate.now().minusYears(30), "Str0ngP@ss!"
        );
        ResponseEntity<?> response = controller.register(req, "198.51.100.5", "JUnit-Agent");
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        verify(auditService, atLeastOnce()).log(eq("registration"), isNull(), eq("198.51.100.5"), eq("JUnit-Agent"), eq(false), any());
    }

    @Test
    void register_invalidEmail_returnsBadRequest_andLogsAudit() {
        PatientRegistrationService registrationService = mock(PatientRegistrationService.class);
        AuditService auditService = mock(AuditService.class);
        com.example.forgetpass.services.ReCaptchaService reCaptchaService = mock(com.example.forgetpass.services.ReCaptchaService.class);
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);
        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);

        when(registrationService.registerAndIssueVerification(anyString(), anyString(), eq("bad@disposable.invalid"), anyString(), any(LocalDate.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("invalid_email"));

        PatientRegistrationController.RegisterRequest req = new PatientRegistrationController.RegisterRequest(
                "Bad", "User", "bad@disposable.invalid", "+1 555-2222", LocalDate.now().minusYears(25), "Str0ngP@ss!"
        );
        ResponseEntity<?> response = controller.register(req, "198.51.100.6", "JUnit-Agent");
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verify(auditService, atLeastOnce()).log(eq("registration"), isNull(), eq("198.51.100.6"), eq("JUnit-Agent"), eq(false), argThat(map -> "invalid_email".equals(map.get("reason"))));
    }

    @Test
    void register_underage_returnsBadRequest_andLogsAudit() {
        PatientRegistrationService registrationService = mock(PatientRegistrationService.class);
        AuditService auditService = mock(AuditService.class);
        com.example.forgetpass.services.ReCaptchaService reCaptchaService = mock(com.example.forgetpass.services.ReCaptchaService.class);
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);
        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);

        when(registrationService.registerAndIssueVerification(anyString(), anyString(), eq("teen@example.com"), anyString(), any(LocalDate.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("underage"));

        PatientRegistrationController.RegisterRequest req = new PatientRegistrationController.RegisterRequest(
                "Young", "User", "teen@example.com", "+1 555-3333", LocalDate.now().minusYears(15), "Str0ngP@ss!"
        );
        ResponseEntity<?> response = controller.register(req, "198.51.100.7", "JUnit-Agent");
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verify(auditService, atLeastOnce()).log(eq("registration"), isNull(), eq("198.51.100.7"), eq("JUnit-Agent"), eq(false), argThat(map -> "underage".equals(map.get("reason"))));
    }

    @Test
    void register_weakPassword_returnsBadRequest_andLogsAudit() {
        PatientRegistrationService registrationService = mock(PatientRegistrationService.class);
        AuditService auditService = mock(AuditService.class);
        com.example.forgetpass.services.ReCaptchaService reCaptchaService = mock(com.example.forgetpass.services.ReCaptchaService.class);
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);
        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);

        when(registrationService.registerAndIssueVerification(anyString(), anyString(), eq("user@example.com"), anyString(), any(LocalDate.class), eq("weak"), anyString()))
                .thenThrow(new IllegalArgumentException("weak_password"));

        PatientRegistrationController.RegisterRequest req = new PatientRegistrationController.RegisterRequest(
                "Weak", "User", "user@example.com", "+1 555-4444", LocalDate.now().minusYears(25), "weak"
        );
        ResponseEntity<?> response = controller.register(req, "198.51.100.8", "JUnit-Agent");
        assertThat(response.getStatusCode().value()).isEqualTo(400);
                verify(auditService, atLeastOnce()).log(eq("registration"), isNull(), eq("198.51.100.8"), eq("JUnit-Agent"), eq(false), argThat(map -> "weak_password".equals(map.get("reason"))));
    }
}
