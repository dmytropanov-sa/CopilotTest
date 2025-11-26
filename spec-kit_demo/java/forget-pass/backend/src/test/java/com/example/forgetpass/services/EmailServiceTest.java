package com.example.forgetpass.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceTest {

    @Test
    void abbreviateNullAndShort() throws Exception {
        EmailService s = new EmailService();
        java.lang.reflect.Method m = EmailService.class.getDeclaredMethod("abbreviate", String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(s, (Object) null));
        assertEquals("hello", m.invoke(s, "hello"));
    }

    @Test
    void dispatchDryRunWhenNoApiKey() throws Exception {
        EmailService s = new EmailService();
        s.sendPasswordChangedConfirmation("nobody@example.com");
    }

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
