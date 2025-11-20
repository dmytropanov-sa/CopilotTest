package com.example.forgetpass.controllers;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.services.AuditService;
import com.example.forgetpass.services.PatientRegistrationService;
import com.example.forgetpass.services.ReCaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientRegistrationControllerSuccessTest {

    MockMvc mockMvc;
    PatientRegistrationService registrationService;
    AuditService auditService;
    ReCaptchaService reCaptchaService;

    @BeforeEach
    void setup() {
        registrationService = mock(PatientRegistrationService.class);
        auditService = mock(AuditService.class);
        reCaptchaService = mock(ReCaptchaService.class);

        PatientRegistrationController controller = new PatientRegistrationController(registrationService, auditService, reCaptchaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void register_success_triggersEmailAndAudit() throws Exception {
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(true);

        Patient p = new Patient();
        p.setPatientId(java.util.UUID.randomUUID());
        p.setEmail("success@example.com");
        when(registrationService.registerAndIssueVerification(anyString(), anyString(), eq("success@example.com"), anyString(), any(LocalDate.class), anyString(), anyString()))
            .thenReturn(p);

        String body = "{\n" +
            "  \"firstName\": \"Alice\",\n" +
            "  \"lastName\": \"Smith\",\n" +
            "  \"email\": \"success@example.com\",\n" +
            "  \"phoneNumber\": \"+1 555-9999\",\n" +
            "  \"dateOfBirth\": \"1990-01-01\",\n" +
            "  \"password\": \"Str0ngP@ss!\"\n" +
            "}";

        mockMvc.perform(post("/api/v1/patients/register")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-ReCaptcha-Token", "good-token")
            .content(body))
            .andExpect(status().isCreated());

        verify(registrationService, times(1)).registerAndIssueVerification(anyString(), anyString(), eq("success@example.com"), anyString(), any(LocalDate.class), anyString(), anyString());
        verify(auditService, atLeastOnce()).log(eq("registration"), eq(p), nullable(String.class), nullable(String.class), eq(true), any());
    }
}
