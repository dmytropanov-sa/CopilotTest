package com.example.forgetpass.services;

import com.example.forgetpass.domain.PasswordResetToken;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import com.example.forgetpass.repositories.PasswordResetTokenRepository;
import com.example.forgetpass.repositories.PatientCredentialRepository;
import com.example.forgetpass.repositories.PatientRepository;
import com.example.forgetpass.util.TokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {
    private final PatientRepository patientRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PatientCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordValidationService passwordValidationService;
    private final AuditService auditService;

    public PasswordResetService(PatientRepository patientRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PatientCredentialRepository credentialRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                PasswordValidationService passwordValidationService,
                                AuditService auditService) {
        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.passwordValidationService = passwordValidationService;
        this.auditService = auditService;
    }

    @Transactional
    public void requestReset(String email, String baseUrl) {
        Optional<Patient> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            // Invalidate previous tokens
            List<PasswordResetToken> active = tokenRepository.findByPatientAndUsedAtIsNull(patient);
            for (PasswordResetToken t : active) {
                t.setUsedAt(Instant.now());
            }
            tokenRepository.saveAll(active);

            String token = TokenUtil.generateToken();
            String hash = TokenUtil.sha256(token);
            PasswordResetToken prt = new PasswordResetToken();
            prt.setPatient(patient);
            prt.setTokenHash(hash);
            prt.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
            // IP/User-Agent no longer captured in rollback
            tokenRepository.save(prt);

            String link = baseUrl + "/reset?token=" + token;
            emailService.sendPasswordResetEmail(patient.getEmail(), link);

            auditService.log("password_reset_request", patient, null, null, true,
                java.util.Map.of("invalidatedTokens", active.size()));
        }
        // Log generic request to prevent enumeration (no patient linkage)
        if (patientOpt.isEmpty()) {
            auditService.log("password_reset_request", null, null, null, true,
                java.util.Map.of("requestedEmailHash", TokenUtil.sha256(email)));
        }
        // Always succeed with generic message to prevent enumeration
    }

    @Transactional
    public boolean validateToken(String token) {
        String hash = TokenUtil.sha256(token);
        Optional<PasswordResetToken> opt = tokenRepository.findByTokenHash(hash);
        return opt.filter(t -> t.getUsedAt() == null && t.getExpiresAt().isAfter(Instant.now())).isPresent();
    }

    @Transactional
    public boolean confirm(String token, String newPassword) {
        if (!passwordValidationService.meetsPolicy(newPassword)) {
            auditService.log("password_reset_confirm", null, null, null, false,
                java.util.Map.of("reason", "weak_password"));
            throw new IllegalArgumentException("weak_password");
        }
        String hash = TokenUtil.sha256(token);
        Optional<PasswordResetToken> opt = tokenRepository.findByTokenHash(hash);
        if (opt.isEmpty()) {
            auditService.log("password_reset_confirm", null, null, null, false,
                java.util.Map.of("reason", "token_missing"));
            return false;
        }
        PasswordResetToken t = opt.get();
        if (t.getUsedAt() != null || t.getExpiresAt().isBefore(Instant.now())) {
            auditService.log("password_reset_confirm", t.getPatient(), null, null, false,
                java.util.Map.of("reason", t.getUsedAt() != null ? "token_used" : "token_expired"));
            return false;
        }

        Patient patient = t.getPatient();
        Optional<PatientCredential> credOpt = credentialRepository.findByPatient(patient);
        if (credOpt.isEmpty()) {
            auditService.log("password_reset_confirm", patient, null, null, false,
                java.util.Map.of("reason", "credential_missing"));
            return false;
        }
        PatientCredential cred = credOpt.get();
        // Prevent reuse of last 5 passwords
        String newHashCandidate = passwordEncoder.encode(newPassword);
        // Compare raw password by matching with stored hashes
        for (String prevHash : cred.getPreviousPasswordHashes()) {
            if (passwordEncoder.matches(newPassword, prevHash)) {
                auditService.log("password_reset_confirm", patient, null, null, false,
                    java.util.Map.of("reason", "password_reuse"));
                throw new IllegalArgumentException("weak_password_or_reuse");
            }
        }
        // Also ensure not equal to current hash
        if (passwordEncoder.matches(newPassword, cred.getPasswordHash())) {
            auditService.log("password_reset_confirm", patient, null, null, false,
                java.util.Map.of("reason", "password_reuse_current"));
            throw new IllegalArgumentException("weak_password_or_reuse");
        }

        // Save previous current hash into history and set new password
        cred.pushPreviousPasswordHash(cred.getPasswordHash(), 5);
        cred.setPasswordHash(newHashCandidate);
        cred.setPasswordChangedAt(Instant.now());
        credentialRepository.save(cred);

        // Invalidate this and any other active tokens
        t.setUsedAt(Instant.now());
        tokenRepository.save(t);
        List<PasswordResetToken> others = tokenRepository.findByPatientAndUsedAtIsNull(patient);
        for (PasswordResetToken ot : others) {
            ot.setUsedAt(Instant.now());
        }
        tokenRepository.saveAll(others);

        emailService.sendPasswordChangedConfirmation(patient.getEmail());
        auditService.log("password_reset_confirm", patient, null, null, true,
            java.util.Map.of());
        return true;
    }
}
