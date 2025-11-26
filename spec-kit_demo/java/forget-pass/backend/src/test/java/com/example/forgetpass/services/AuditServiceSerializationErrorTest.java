package com.example.forgetpass.services;

import com.example.forgetpass.domain.AuthenticationAuditLog;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.repositories.AuthenticationAuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class AuditServiceSerializationErrorTest {

    @Test
    void log_handlesJsonProcessingException_andStillSaves() throws Exception {
        AuthenticationAuditLogRepository repo = mock(AuthenticationAuditLogRepository.class);
        AuditService svc = new AuditService(repo);

        // replace private final objectMapper with a mock that throws
        ObjectMapper failing = mock(ObjectMapper.class);
        when(failing.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {}
        );

        java.lang.reflect.Field f = AuditService.class.getDeclaredField("objectMapper");
        f.setAccessible(true);
        f.set(svc, failing);

        Patient p = new Patient();
        p.setEmail("x@y.z");

        assertThatCode(() -> svc.log("LOGIN", p, "1.2.3.4", "ua", true, Map.of("k", "v")))
                .doesNotThrowAnyException();

        verify(repo).save(any(AuthenticationAuditLog.class));
    }
}
