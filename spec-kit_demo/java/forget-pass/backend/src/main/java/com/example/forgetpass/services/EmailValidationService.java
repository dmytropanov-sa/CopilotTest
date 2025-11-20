package com.example.forgetpass.services;

import com.example.forgetpass.util.DisposableEmailChecker;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class EmailValidationService {
    // RFC 5322 simplified pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    // Keep a minimal in-code set; use DisposableEmailChecker for a fuller list
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "10minutemail.com", "guerrillamail.com", "tempmail.org", "mailinator.com"
    );

    public boolean isValidFormat(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isDisposable(String email) {
        if (email == null) return true;
        DisposableEmailChecker checker = new DisposableEmailChecker();
        return checker.isDisposable(email);
    }
}
