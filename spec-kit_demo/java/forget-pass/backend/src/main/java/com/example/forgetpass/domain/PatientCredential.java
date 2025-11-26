package com.example.forgetpass.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "patient_credentials")
public class PatientCredential {

    @Id
    @Column(name = "credential_id", nullable = false, updatable = false)
    private UUID credentialId;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt = Instant.now();

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "previous_password_hashes")
    private String previousPasswordHashesJson;

    @PrePersist
    public void onCreate() {
        if (credentialId == null) {
            credentialId = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getCredentialId() { return credentialId; }
    public void setCredentialId(UUID credentialId) { this.credentialId = credentialId; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Instant getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getPreviousPasswordHashesJson() { return previousPasswordHashesJson; }
    public void setPreviousPasswordHashesJson(String previousPasswordHashesJson) { this.previousPasswordHashesJson = previousPasswordHashesJson; }

    // JSON-serialized helpers for previous password hashes (most-recent first)
    public List<String> getPreviousPasswordHashes() {
        if (previousPasswordHashesJson == null || previousPasswordHashesJson.isBlank()) return new ArrayList<>();
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(previousPasswordHashesJson, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setPreviousPasswordHashes(List<String> hashes) {
        try {
            ObjectMapper om = new ObjectMapper();
            this.previousPasswordHashesJson = om.writeValueAsString(hashes == null ? new ArrayList<>() : hashes);
        } catch (Exception e) {
            this.previousPasswordHashesJson = null;
        }
    }

    public void pushPreviousPasswordHash(String hash, int maxHistory) {
        List<String> list = getPreviousPasswordHashes();
        if (hash != null && !hash.isBlank()) {
            list.add(0, hash);
            if (list.size() > maxHistory) {
                list = list.subList(0, maxHistory);
            }
        }
        setPreviousPasswordHashes(list);
    }
}
