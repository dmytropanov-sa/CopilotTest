package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReCaptchaServiceTest {

    @Test
    void validate_returnsTrue_whenSecretMissing() {
        ReCaptchaClient client = mock(ReCaptchaClient.class);
        ReCaptchaService svc = new ReCaptchaService(client);
        // Ensure env not set
        assertThat(svc.validate("any", "register")).isTrue();
        verifyNoInteractions(client);
    }

    @Test
    void validate_false_on_action_mismatch_or_low_score() throws Exception {
        ReCaptchaClient client = mock(ReCaptchaClient.class);
        ReCaptchaResponse resp = new ReCaptchaResponse();
        resp.setSuccess(true);
        resp.setAction("login");
        resp.setScore(0.1);
        when(client.verify(anyString(), anyString())).thenReturn(resp);

        // set property for this test only
        try {
            ReCaptchaService svc = new ReCaptchaService(client) {
                { System.setProperty("RECAPTCHA_SECRET", "x"); }
            };
            boolean ok = svc.validate("token", "register");
            assertThat(ok).isFalse();
        } finally {
            System.clearProperty("RECAPTCHA_SECRET");
        }
    }
}
