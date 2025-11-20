package com.example.forgetpass.services;

import com.example.forgetpass.domain.EmailVerificationToken;
import com.example.forgetpass.domain.PasswordResetToken;
import com.example.forgetpass.repositories.EmailVerificationTokenRepository;
import com.example.forgetpass.repositories.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenCleanupService {
    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public TokenCleanupService(PasswordResetTokenRepository passwordResetTokenRepository,
                               EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    // Run once a day at 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredAndUsedTokens() {
        Instant now = Instant.now();

        // Cleanup password reset tokens: remove tokens that are used or expired
        List<PasswordResetToken> allPrt = passwordResetTokenRepository.findAll();
        int removedPrt = 0;
        for (PasswordResetToken t : allPrt) {
            if (t.getUsedAt() != null || t.getExpiresAt() == null || t.getExpiresAt().isBefore(now)) {
                passwordResetTokenRepository.delete(t);
                removedPrt++;
            }
        }
        log.info("TokenCleanupService: removed {} password reset tokens", removedPrt);

        // Cleanup email verification tokens: remove expired tokens
        List<EmailVerificationToken> allEvt = emailVerificationTokenRepository.findAll();
        int removedEvt = 0;
        for (EmailVerificationToken t : allEvt) {
            if (t.getExpiresAt() == null || t.getExpiresAt().isBefore(now)) {
                emailVerificationTokenRepository.delete(t);
                removedEvt++;
            }
        }
        log.info("TokenCleanupService: removed {} email verification tokens", removedEvt);
    }
}
