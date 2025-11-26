package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailValidationServiceTest {
    private final EmailValidationService svc = new EmailValidationService();

    @Test
    void isValidFormat_handlesNullAndValidAndInvalid() {
        assertThat(svc.isValidFormat(null)).isFalse();
        assertThat(svc.isValidFormat("alice@example.com")).isTrue();
        assertThat(svc.isValidFormat("bad-email@com")).isFalse();
        assertThat(svc.isValidFormat("UPPER.CASE@Example.COM")).isTrue();
    }

    @Test
    void isDisposable_detectsDisposableAndNonDisposable() {
        // null treated as disposable by implementation
        assertThat(svc.isDisposable(null)).isTrue();

        // common disposable domain from in-code list
        assertThat(svc.isDisposable("user@mailinator.com")).isTrue();

        // normal domain
        assertThat(svc.isDisposable("person@mycompany.org")).isFalse();
    }
}
