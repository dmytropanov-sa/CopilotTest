package com.example.forgetpass.services;

import com.example.forgetpass.domain.EmailVerificationToken;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.repositories.EmailVerificationTokenRepository;
import com.example.forgetpass.repositories.PatientRepository;
import com.example.forgetpass.util.TokenUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EmailVerificationService {
    private final PatientRepository patientRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    public EmailVerificationService(PatientRepository patientRepository,
                                    EmailVerificationTokenRepository tokenRepository,
                                    EmailService emailService,
                                    AuditService auditService) {
        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @Transactional
    public void issueVerification(Patient patient, String baseUrl) {
        String token = TokenUtil.generateToken();
        String hash = TokenUtil.sha256(token);
        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setPatient(patient);
        evt.setTokenHash(hash);
        evt.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        tokenRepository.save(evt);
        String link = baseUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(patient.getEmail(), link);
        auditService.log("verification_issued", patient, null, null, true, java.util.Map.of());
    }

    @Transactional
    public boolean verify(String token) {
        String hash = TokenUtil.sha256(token);
        Optional<EmailVerificationToken> opt = tokenRepository.findByTokenHash(hash);
        if (opt.isEmpty()) return false;
        EmailVerificationToken evt = opt.get();
        if (evt.getVerifiedAt() != null || evt.getExpiresAt().isBefore(Instant.now())) {
            auditService.log("verification_confirm", evt.getPatient(), null, null, false,
                java.util.Map.of("reason", evt.getVerifiedAt() != null ? "already_verified" : "expired"));
            return false;
        }
        evt.setVerifiedAt(Instant.now());
        tokenRepository.save(evt);

        Patient patient = evt.getPatient();
        patient.setAccountStatus("active");
        patientRepository.save(patient);
        auditService.log("verification_confirm", patient, null, null, true, java.util.Map.of());
        return true;
    }

    @Transactional
    public boolean resend(String email, String baseUrl) {
        Optional<Patient> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isEmpty()) return false;
        Patient patient = patientOpt.get();

        // Limit to maximum 3 resends per 24 hours (fresh tokens)
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        long createdLast24h = tokenRepository.countByPatientAndCreatedAtAfter(patient, cutoff);
        // First issuance may have happened already; allow up to 3 resends -> total up to 4 tokens in 24h
        if (createdLast24h >= 4) {
            auditService.log("verification_resend", patient, null, null, false, java.util.Map.of("reason", "limit_exceeded"));
            return false;
        }

        issueVerification(patient, baseUrl);
        auditService.log("verification_resend", patient, null, null, true, java.util.Map.of());
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<EmailVerificationToken> latestToken(Patient patient) {
        List<EmailVerificationToken> tokens = tokenRepository.findByPatient(patient);
        return tokens.stream().max(Comparator.comparing(EmailVerificationToken::getCreatedAt));
    }
}
