package com.example.forgetpass.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientCredentialTest {

    @Test
    void getAndSetPreviousPasswordHashesRoundtrip() {
        PatientCredential pc = new PatientCredential();
        pc.setPreviousPasswordHashes(List.of("h1", "h2"));
        List<String> hashes = pc.getPreviousPasswordHashes();
        assertEquals(2, hashes.size());
        assertEquals("h1", hashes.get(0));
        assertEquals("h2", hashes.get(1));
    }

    @Test
    void pushPreviousPasswordHashAddsAndCapsHistory() {
        PatientCredential pc = new PatientCredential();
        pc.setPreviousPasswordHashes(List.of("a","b","c","d"));
        pc.pushPreviousPasswordHash("new", 3);
        List<String> hashes = pc.getPreviousPasswordHashes();
        assertEquals("new", hashes.get(0));
        assertTrue(hashes.size() <= 3);
    }

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
        pc.onUpdate();

        assertThat(pc.getUpdatedAt()).isAfterOrEqualTo(initial);
    }
}