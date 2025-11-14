package com.example.forgetpass.controllers;

import com.example.forgetpass.services.PasswordResetService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    public record RequestDto(@Email String email) {}
    public record ConfirmDto(@NotBlank String token, @NotBlank String newPassword) {}
    public record ValidateDto(@NotBlank String token) {}

    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody RequestDto dto) {
        String baseUrl = "http://localhost:8080/api/v1/patients/password-reset"; // dev; frontend should provide proper base
        passwordResetService.requestReset(dto.email(), baseUrl);
        return ResponseEntity.ok(Map.of("message", "If account exists, reset instructions sent"));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateDto dto) {
        boolean valid = passwordResetService.validateToken(dto.token());
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmDto dto) {
        boolean ok = passwordResetService.confirm(dto.token(), dto.newPassword());
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
