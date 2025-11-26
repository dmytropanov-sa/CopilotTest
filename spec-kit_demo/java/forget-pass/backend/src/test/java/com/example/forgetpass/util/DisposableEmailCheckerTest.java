package com.example.forgetpass.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Set;

class DisposableEmailCheckerTest {

    @Test
    void nullAndMalformedEmailsAreNotDisposable() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertFalse(c.isDisposable(null));
        assertFalse(c.isDisposable(""));
        assertFalse(c.isDisposable("no-at-symbol"));
    }

    @Test
    void blacklistedDomainIsDisposable() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertTrue(c.isDisposable("user@mailinator.com") || c.isDisposable("user@trashmail.com") || c.isDisposable("user@tempmail.com"));
    }

    @Test
    void subdomainMatchesBaseDomain() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        String email = "foo@sub.mailinator.com";
        assertTrue(c.isDisposable(email));
    }

    @Test
    void extraDomainsAreAcceptedCaseInsensitive() {
        DisposableEmailChecker c = new DisposableEmailChecker(Set.of("EXTRA-DOMAIN.COM"));
        assertTrue(c.isDisposable("a@extra-domain.com"));
        assertTrue(c.isDisposable("b@EXTRA-domain.com"));
    }

    @Test
    void detectsCommonDisposableDomains() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertTrue(c.isDisposable("user@mailinator.com") || c.isDisposable("x@yopmail.com") || c.isDisposable("test@sub.maildrop.cc"));
    }

    @Test
    void acceptsNormalDomains() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertFalse(c.isDisposable("alice@example.com"));
        assertFalse(c.isDisposable("bob@mycompany.co.uk"));
    }

    @Test
    void handlesNullAndInvalid() {
        DisposableEmailChecker c = new DisposableEmailChecker();
        assertFalse(c.isDisposable(null));
        assertFalse(c.isDisposable("not-an-email"));
    }
}
