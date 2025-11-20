package com.example.forgetpass.controllers;

import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.services.PatientRegistrationService;
import com.example.forgetpass.services.EmailVerificationService;
import com.example.forgetpass.services.AuditService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientRegistrationController {

    private final PatientRegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;
    private final AuditService auditService;
    private final com.example.forgetpass.services.ReCaptchaService reCaptchaService;

    public PatientRegistrationController(PatientRegistrationService registrationService,
                                         EmailVerificationService emailVerificationService,
                                         AuditService auditService,
                                         com.example.forgetpass.services.ReCaptchaService reCaptchaService) {
        this.registrationService = registrationService;
        this.emailVerificationService = emailVerificationService;
        this.auditService = auditService;
        this.reCaptchaService = reCaptchaService;
    }

    public record RegisterRequest(@NotBlank String firstName,
                                  @NotBlank String lastName,
                                  @Email String email,
                                  String phoneNumber,
                                  @NotNull LocalDate dateOfBirth,
                                  @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req,
                                      @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                      @RequestHeader(value = "User-Agent", required = false) String ua,
                                      @RequestHeader(value = "X-ReCaptcha-Token", required = false) String captchaToken) {
        // require reCAPTCHA validation
        if (!reCaptchaService.validate(captchaToken == null ? "" : captchaToken, "register")) {
            auditService.log("registration", null, xff, ua, false,
                java.util.Map.of("reason", "recaptcha_failed"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "reCAPTCHA validation failed"));
        }
        try {
            Patient p = registrationService.register(
                req.firstName(), req.lastName(), req.email(), req.phoneNumber(), req.dateOfBirth(), req.password()
            );
            // Issue verification email with 24h token
            String baseUrl;
            try {
                baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            } catch (IllegalStateException ex) {
                baseUrl = "http://localhost:8080";
            }
            emailVerificationService.issueVerification(p, baseUrl);
            auditService.log("registration", p, xff, ua, true,
                java.util.Map.of("email", p.getEmail()));
            java.util.Map<String,Object> body = new java.util.HashMap<>();
            body.put("patientId", p.getPatientId());
            body.put("email", p.getEmail());
            body.put("status", p.getAccountStatus());
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalStateException e) {
            if ("email_already_exists".equals(e.getMessage())) {
                auditService.log("registration", null, xff, ua, false,
                    java.util.Map.of("reason", "email_already_exists", "emailHash", com.example.forgetpass.util.TokenUtil.sha256(req.email())));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "An account with this email already exists"
                ));
            }
            auditService.log("registration", null, xff, ua, false,
                java.util.Map.of("reason", "other_failure"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "message", "Registration failed"
            ));
        } catch (IllegalArgumentException e) {
            String msg;
            switch (String.valueOf(e.getMessage())) {
                case "invalid_email":
                    msg = "Please use a permanent email address for registration.";
                    break;
                case "underage":
                    msg = "Patients must be at least 18 years old.";
                    break;
                case "weak_password":
                    msg = "Password does not meet security requirements.";
                    break;
                default:
                    msg = "Invalid input";
            }
            auditService.log("registration", null, xff, ua, false,
                java.util.Map.of("reason", e.getMessage(), "emailHash", com.example.forgetpass.util.TokenUtil.sha256(req.email())));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", msg));
        }
    }

    // Backwards-compatible overload used by existing unit tests
    public ResponseEntity<?> register(RegisterRequest req, String xff, String ua) {
        return register(req, xff, ua, null);
    }
}
