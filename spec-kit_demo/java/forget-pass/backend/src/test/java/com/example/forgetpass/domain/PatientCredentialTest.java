package com.example.forgetpass.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PatientCredentialTest {

    @Test
    void onCreate_setsCredentialIdAndTimestamps() {
        PatientCredential pc = new PatientCredential();
        pc.onCreate();

        assertThat(pc.getCredentialId()).isNotNull();
        assertThat(pc.getCreatedAt()).isNotNull();
        assertThat(pc.getUpdatedAt()).isEqualTo(pc.getCreatedAt());
    }

    @Test
    void onUpdate_setsUpdatedAt() {
        PatientCredential pc = new PatientCredential();
        Instant initial = pc.getUpdatedAt();
        // Simulate some time passing
        pc.onUpdate();

        assertThat(pc.getUpdatedAt()).isAfterOrEqualTo(initial);
    }

    // Note: Getters and setters are not tested as they are auto-generated and trivial
}