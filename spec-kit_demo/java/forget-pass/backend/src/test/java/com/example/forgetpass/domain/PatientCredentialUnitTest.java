package com.example.forgetpass.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatientCredentialUnitTest {
    @Test
    void previousPasswordHashes_roundtripAndPushAndTrim() {
        PatientCredential pc = new PatientCredential();
        assertThat(pc.getPreviousPasswordHashes()).isEmpty();

        pc.pushPreviousPasswordHash("hash1", 3);
        pc.pushPreviousPasswordHash("hash2", 3);
        pc.pushPreviousPasswordHash("hash3", 3);
        pc.pushPreviousPasswordHash("hash4", 3);

        List<String> hashes = pc.getPreviousPasswordHashes();
        assertThat(hashes).containsExactly("hash4", "hash3", "hash2");

        // malformed JSON in field should be handled gracefully
        pc.setPreviousPasswordHashesJson("not-a-json");
        assertThat(pc.getPreviousPasswordHashes()).isEmpty();
    }
}
