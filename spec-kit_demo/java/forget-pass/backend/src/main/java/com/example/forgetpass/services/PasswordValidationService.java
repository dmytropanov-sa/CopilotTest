package com.example.forgetpass.services;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PasswordValidationService {
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL = Pattern.compile(".*[!@#$%^&*()_+=\\-\\[\\]{};:'\"\\\\|,.<>/?].*");

    private static final Set<String> COMMON = Set.of(
        "password", "123456", "123456789", "qwerty", "letmein",
        "welcome", "admin", "iloveyou", "login", "abc123"
    );

    public boolean meetsPolicy(String password) {
        if (password == null || password.length() < 12) return false;
        if (!UPPER.matcher(password).matches()) return false;
        if (!LOWER.matcher(password).matches()) return false;
        if (!DIGIT.matcher(password).matches()) return false;
        if (!SPECIAL.matcher(password).matches()) return false;
        String lc = password.toLowerCase();
        if (COMMON.contains(lc)) return false;
        if (lc.contains("password")) return false;
        return true;
    }
}
