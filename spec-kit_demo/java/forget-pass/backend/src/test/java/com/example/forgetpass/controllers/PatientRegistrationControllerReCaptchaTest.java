package com.example.forgetpass.controllers;

import com.example.forgetpass.services.AuditService;
import com.example.forgetpass.services.EmailVerificationService;
import com.example.forgetpass.services.PatientRegistrationService;
import com.example.forgetpass.services.ReCaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PatientRegistrationControllerReCaptchaTest {

    MockMvc mockMvc;
    PatientRegistrationService registrationService;
    EmailVerificationService emailVerificationService;
    AuditService auditService;
    ReCaptchaService reCaptchaService;

    @BeforeEach
    void setup() {
        registrationService = mock(PatientRegistrationService.class);
        emailVerificationService = mock(EmailVerificationService.class);
        auditService = mock(AuditService.class);
        reCaptchaService = mock(ReCaptchaService.class);

        PatientRegistrationController controller = new PatientRegistrationController(registrationService, emailVerificationService, auditService, reCaptchaService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void register_rejects_when_recaptcha_fails() throws Exception {
        when(reCaptchaService.validate(anyString(), anyString())).thenReturn(false);

        String body = "{\n" +
            "  \"firstName\": \"John\",\n" +
            "  \"lastName\": \"Doe\",\n" +
            "  \"email\": \"john.doe@example.com\",\n" +
            "  \"phoneNumber\": \"12345\",\n" +
            "  \"dateOfBirth\": \"1990-01-01\",\n" +
            "  \"password\": \"Str0ng!Pass\"\n" +
            "}";

        mockMvc.perform(post("/api/v1/patients/register")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-ReCaptcha-Token", "bad-token")
            .content(body))
            .andExpect(status().isBadRequest());

        verify(auditService).log(eq("registration"), isNull(), nullable(String.class), nullable(String.class), eq(false), anyMap());
    }
}