package com.example.forgetpass.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReCaptchaServiceTest {
    private ReCaptchaClient client;
    private ReCaptchaService service;

    @BeforeEach
    void setUp() {
        client = Mockito.mock(ReCaptchaClient.class);
        service = new ReCaptchaService(client);
        System.clearProperty("RECAPTCHA_SECRET");
        System.clearProperty("RECAPTCHA_MIN_SCORE");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("RECAPTCHA_SECRET");
        System.clearProperty("RECAPTCHA_MIN_SCORE");
    }

    @Test
    void validate_skipsWhenSecretMissing() {
        assertThat(service.validate("any-token", "act")).isTrue();
        verifyNoInteractions(client);
    }

    @Test
    void validate_returnsFalseOnVerifyFailure() throws Exception {
        System.setProperty("RECAPTCHA_SECRET", "s");
        ReCaptchaResponse resp = new ReCaptchaResponse();
        resp.setSuccess(false);
        when(client.verify(anyString(), anyString())).thenReturn(resp);

        assertThat(service.validate("t", "a")).isFalse();
    }

    @Test
    void validate_returnsFalseOnActionMismatchOrLowScore() throws Exception {
        System.setProperty("RECAPTCHA_SECRET", "s");
        ReCaptchaResponse resp = new ReCaptchaResponse();
        resp.setSuccess(true);
        resp.setAction("login");
        resp.setScore(0.1);
        when(client.verify(anyString(), anyString())).thenReturn(resp);

        assertThat(service.validate("token", "register")).isFalse();
    }

    @Test
    void validate_respectsMinScoreFromProperty_and_handlesMalformed() throws Exception {
        System.setProperty("RECAPTCHA_SECRET", "s");
        System.setProperty("RECAPTCHA_MIN_SCORE", "0.9");

        ReCaptchaResponse resp = new ReCaptchaResponse();
        resp.setSuccess(true);
        resp.setAction("act");
        resp.setScore(0.85);
        when(client.verify(anyString(), anyString())).thenReturn(resp);

        assertThat(service.validate("t", "act")).isFalse();

        // malformed property -> fallback to default 0.5
        System.setProperty("RECAPTCHA_MIN_SCORE", "bad");
        assertThat(service.validate("t", "act")).isTrue();
    }

    @Test
    void validate_handlesClientException() throws Exception {
        System.setProperty("RECAPTCHA_SECRET", "s");
        when(client.verify(anyString(), anyString())).thenThrow(new RuntimeException("boom"));

        assertThat(service.validate("t", "a")).isFalse();
    }
}
