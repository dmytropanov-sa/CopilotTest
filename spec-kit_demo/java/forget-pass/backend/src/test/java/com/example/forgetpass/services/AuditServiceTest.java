package com.example.forgetpass.services;

import com.example.forgetpass.domain.AuthenticationAuditLog;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.repositories.AuthenticationAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditServiceTest {

    @Test
    void log_serializesMetadata_andSavesEntry() {
        AuthenticationAuditLogRepository repo = mock(AuthenticationAuditLogRepository.class);
        AuditService svc = new AuditService(repo);

        Patient p = new Patient();
        p.setEmail("me@example.com");

        Map<String, Object> meta = Map.of("k", "v");
        svc.log("LOGIN", p, "1.2.3.4", "ua", true, meta);

        ArgumentCaptor<AuthenticationAuditLog> cap = ArgumentCaptor.forClass(AuthenticationAuditLog.class);
        verify(repo).save(cap.capture());
        AuthenticationAuditLog entry = cap.getValue();
        assertThat(entry.getEventType()).isEqualTo("LOGIN");
        assertThat(entry.getPatient()).isSameAs(p);
        assertThat(entry.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(entry.isSuccess()).isTrue();
        assertThat(entry.getMetadataJson()).contains("k").contains("v");
    }
}
