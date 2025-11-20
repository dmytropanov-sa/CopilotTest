package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceTest {

    @Test
    void dryRunMode_noApiKey_doesNotThrow() {
        EmailService svc = new EmailService();
        assertThatCode(() -> svc.sendVerificationEmail("alice@example.com", "https://app.local/verify?t=abc"))
            .doesNotThrowAnyException();
        assertThatCode(() -> svc.sendPasswordResetEmail("bob@example.com", "https://app.local/reset?t=xyz"))
            .doesNotThrowAnyException();
        assertThatCode(() -> svc.sendPasswordChangedConfirmation("carol@example.com"))
            .doesNotThrowAnyException();
    }
}
