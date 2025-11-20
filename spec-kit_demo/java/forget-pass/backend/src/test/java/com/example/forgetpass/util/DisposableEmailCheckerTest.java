package com.example.forgetpass.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisposableEmailCheckerTest {

    @Test
    void detectsCommonDisposableDomains() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertThat(c.isDisposable("user@mailinator.com")).isTrue();
        assertThat(c.isDisposable("x@yopmail.com")).isTrue();
        assertThat(c.isDisposable("test@sub.maildrop.cc")).isTrue();
    }

    @Test
    void acceptsNormalDomains() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertThat(c.isDisposable("alice@example.com")).isFalse();
        assertThat(c.isDisposable("bob@mycompany.co.uk")).isFalse();
    }

    @Test
    void handlesNullAndInvalid() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertThat(c.isDisposable(null)).isFalse();
        assertThat(c.isDisposable("not-an-email")).isFalse();
    }
}
