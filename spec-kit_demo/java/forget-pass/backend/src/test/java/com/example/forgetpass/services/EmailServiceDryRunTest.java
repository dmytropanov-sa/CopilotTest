package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatCode;

public class EmailServiceDryRunTest {

    @Test
    void dispatch_is_dry_run_when_api_key_missing() throws Exception {
        EmailService svc = new EmailService();

        Field f = EmailService.class.getDeclaredField("apiKey");
        f.setAccessible(true);
        f.set(svc, "");

        assertThatCode(() -> svc.sendVerificationEmail("x@example.com", "link"))
                .doesNotThrowAnyException();

        assertThatCode(() -> svc.sendPasswordResetEmail("x@example.com", "link"))
                .doesNotThrowAnyException();

        assertThatCode(() -> svc.sendPasswordChangedConfirmation("x@example.com"))
                .doesNotThrowAnyException();
    }
}
