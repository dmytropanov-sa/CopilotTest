package com.example.forgetpass.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String to, String link) {
        log.info("[DEV] Send verification email to {}: {}", to, link);
    }

    public void sendPasswordResetEmail(String to, String link) {
        log.info("[DEV] Send password reset email to {}: {}", to, link);
    }

    public void sendPasswordChangedConfirmation(String to) {
        log.info("[DEV] Send password changed confirmation to {}", to);
    }
}
