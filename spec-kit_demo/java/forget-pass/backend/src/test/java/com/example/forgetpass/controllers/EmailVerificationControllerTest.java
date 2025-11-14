package com.example.forgetpass.controllers;

import com.example.forgetpass.services.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailVerificationControllerTest {

    @Test
    void verify_returnsOk_onSuccess() {
        EmailVerificationService service = mock(EmailVerificationService.class);
        EmailVerificationController controller = new EmailVerificationController(service);
        when(service.verify("tok")).thenReturn(true);
        ResponseEntity<?> resp = controller.verify(new EmailVerificationController.VerifyRequest("tok"));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void verify_returnsBadRequest_onFailure() {
        EmailVerificationService service = mock(EmailVerificationService.class);
        EmailVerificationController controller = new EmailVerificationController(service);
        when(service.verify("bad")).thenReturn(false);
        ResponseEntity<?> resp = controller.verify(new EmailVerificationController.VerifyRequest("bad"));
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void resend_returnsOk_whenSent() {
        EmailVerificationService service = mock(EmailVerificationService.class);
        EmailVerificationController controller = new EmailVerificationController(service);
        when(service.resend(eq("user@example.com"), anyString())).thenReturn(true);
        ResponseEntity<?> resp = controller.resend(new EmailVerificationController.ResendRequest("user@example.com"));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void resend_returnsBadRequest_whenLimitExceeded() {
        EmailVerificationService service = mock(EmailVerificationService.class);
        EmailVerificationController controller = new EmailVerificationController(service);
        when(service.resend(eq("user@example.com"), anyString())).thenReturn(false);
        ResponseEntity<?> resp = controller.resend(new EmailVerificationController.ResendRequest("user@example.com"));
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}
