package com.example.forgetpass.controllers;

import com.example.forgetpass.services.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    @Test
    void request_returnsGenericMessage_andInvokesService() {
        PasswordResetService svc = mock(PasswordResetService.class);
        PasswordResetController controller = new PasswordResetController(svc);
        PasswordResetController.RequestDto dto = new PasswordResetController.RequestDto("user@example.com");
        ResponseEntity<?> resp = controller.request(dto);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(svc, times(1)).requestReset(eq("user@example.com"), anyString());
    }

    @Test
    void validateToken_returnsOk_withValidity() {
        PasswordResetService svc = mock(PasswordResetService.class);
        PasswordResetController controller = new PasswordResetController(svc);
        when(svc.validateToken("tok")).thenReturn(true);
        ResponseEntity<?> resp = controller.validateToken(new PasswordResetController.ValidateDto("tok"));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void confirm_returnsBadRequest_onFailure_andOk_onSuccess() {
        PasswordResetService svc = mock(PasswordResetService.class);
        PasswordResetController controller = new PasswordResetController(svc);
        when(svc.confirm("bad", "NewP@ssw0rd!"))
            .thenReturn(false);
        ResponseEntity<?> bad = controller.confirm(new PasswordResetController.ConfirmDto("bad", "NewP@ssw0rd!"));
        assertThat(bad.getStatusCode().value()).isEqualTo(400);

        when(svc.confirm("good", "NewP@ssw0rd!"))
            .thenReturn(true);
        ResponseEntity<?> ok = controller.confirm(new PasswordResetController.ConfirmDto("good", "NewP@ssw0rd!"));
        assertThat(ok.getStatusCode().value()).isEqualTo(200);
    }
}
