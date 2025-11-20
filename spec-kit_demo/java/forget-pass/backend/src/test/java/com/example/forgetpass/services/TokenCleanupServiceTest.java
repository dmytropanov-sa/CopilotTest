package com.example.forgetpass.services;

import com.example.forgetpass.domain.EmailVerificationToken;
import com.example.forgetpass.domain.PasswordResetToken;
import com.example.forgetpass.repositories.EmailVerificationTokenRepository;
import com.example.forgetpass.repositories.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class TokenCleanupServiceTest {

    @Test
    void cleanup_removesExpiredAndUsedTokens() {
        PasswordResetTokenRepository prtRepo = mock(PasswordResetTokenRepository.class);
        EmailVerificationTokenRepository evtRepo = mock(EmailVerificationTokenRepository.class);

        PasswordResetToken good = new PasswordResetToken();
        good.setExpiresAt(Instant.now().plusSeconds(3600));

        PasswordResetToken expired = new PasswordResetToken();
        expired.setExpiresAt(Instant.now().minusSeconds(60));

        PasswordResetToken used = new PasswordResetToken();
        used.setExpiresAt(Instant.now().plusSeconds(3600));
        used.setUsedAt(Instant.now().minusSeconds(10));

        when(prtRepo.findAll()).thenReturn(List.of(good, expired, used));

        EmailVerificationToken evtGood = new EmailVerificationToken();
        evtGood.setExpiresAt(Instant.now().plusSeconds(3600));
        EmailVerificationToken evtExpired = new EmailVerificationToken();
        evtExpired.setExpiresAt(Instant.now().minusSeconds(100));
        when(evtRepo.findAll()).thenReturn(List.of(evtGood, evtExpired));

        TokenCleanupService svc = new TokenCleanupService(prtRepo, evtRepo, "0 0 3 * * *");
        svc.cleanupExpiredAndUsedTokens();

        verify(prtRepo, times(1)).delete(expired);
        verify(prtRepo, times(1)).delete(used);
        verify(prtRepo, times(0)).delete(good);

        verify(evtRepo, times(1)).delete(evtExpired);
        verify(evtRepo, times(0)).delete(evtGood);
    }
}
