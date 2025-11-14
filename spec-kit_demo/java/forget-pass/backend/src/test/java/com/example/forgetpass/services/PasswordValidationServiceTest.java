package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidationServiceTest {

    private final PasswordValidationService svc = new PasswordValidationService();

    @Test
    void rejectsShortOrWeak() {
        assertFalse(svc.meetsPolicy("short1!A"));
        assertFalse(svc.meetsPolicy("passwordPASSWORD12!")); // common
        assertFalse(svc.meetsPolicy("alllowercase12!"));
        assertFalse(svc.meetsPolicy("ALLUPPERCASE12!"));
        assertFalse(svc.meetsPolicy("NoDigits!!!!!!!!!"));
        assertFalse(svc.meetsPolicy("NoSpecials123456"));
    }

    @Test
    void acceptsStrong() {
        assertTrue(svc.meetsPolicy("Str0ng!Passw0rd"));
    }
}
