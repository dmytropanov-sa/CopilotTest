package com.example.forgetpass.controllers;

import com.example.forgetpass.services.EmailVerificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    public record VerifyRequest(@NotBlank String token) {}
    public record ResendRequest(@Email String email) {}

    @PostMapping("/verify-email")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest req) {
        boolean ok = emailVerificationService.verify(req.token());
        if (ok) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "This verification link has expired or is invalid"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resend(@RequestBody ResendRequest req) {
        String baseUrl;
        try {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (IllegalStateException ex) {
            baseUrl = "http://localhost:8080";
        }
        boolean sent = emailVerificationService.resend(req.email(), baseUrl);
        if (sent) {
            return ResponseEntity.ok(Map.of("message", "Verification email sent"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Resend limit reached. Please try again later."));
    }
}
