package com.example.forgetpass.services;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.domain.PatientCredential;
import com.example.forgetpass.repositories.PatientRepository;
import com.example.forgetpass.repositories.PatientCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PatientRegistrationService {

    private final PatientRepository patientRepository;
    private final PatientCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailValidationService emailValidationService;
    private final PasswordValidationService passwordValidationService;
    private final EmailVerificationService emailVerificationService;
    private final AuditService auditService;

    public PatientRegistrationService(PatientRepository patientRepository,
                                      PatientCredentialRepository credentialRepository,
                                      PasswordEncoder passwordEncoder,
                                      EmailValidationService emailValidationService,
                                      PasswordValidationService passwordValidationService,
                                      EmailVerificationService emailVerificationService,
                                      AuditService auditService) {
        this.patientRepository = patientRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailValidationService = emailValidationService;
        this.passwordValidationService = passwordValidationService;
        this.emailVerificationService = emailVerificationService;
        this.auditService = auditService;
    }

    @Transactional
    public Patient register(String firstName, String lastName, String email, String phone, LocalDate dob, String rawPassword) {
        if (!emailValidationService.isValidFormat(email) || emailValidationService.isDisposable(email)) {
            throw new IllegalArgumentException("invalid_email");
        }
        if (!isAdult(dob)) {
            throw new IllegalArgumentException("underage");
        }
        if (!passwordValidationService.meetsPolicy(rawPassword)) {
            throw new IllegalArgumentException("weak_password");
        }
        Optional<Patient> existing = patientRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalStateException("email_already_exists");
        }
        Patient p = new Patient();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setPhoneNumber(phone);
        p.setDateOfBirth(dob);
        Patient saved = patientRepository.save(p);

        PatientCredential cred = new PatientCredential();
        cred.setPatient(saved);
        cred.setPasswordHash(passwordEncoder.encode(rawPassword));
        credentialRepository.save(cred);

        return saved;
    }

    @Transactional
    public Patient registerAndIssueVerification(String firstName, String lastName, String email, String phone, LocalDate dob, String rawPassword, String baseUrl) {
        Patient saved = register(firstName, lastName, email, phone, dob, rawPassword);
        // Issue verification email and audit the registration
        try {
            emailVerificationService.issueVerification(saved, baseUrl);
            auditService.log("registration", saved, null, null, true, java.util.Map.of("email", saved.getEmail()));
        } catch (Exception ex) {
            // Do not fail registration on email issues; log and proceed
            auditService.log("registration", saved, null, null, false, java.util.Map.of("reason", "email_issue", "error", ex.getMessage()));
        }
        return saved;
    }

    private boolean isAdult(LocalDate dob) {
        if (dob == null) return false;
        LocalDate now = LocalDate.now();
        return !dob.isAfter(now.minusYears(18));
    }
}
