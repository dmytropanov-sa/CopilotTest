package com.example.forgetpass.services;

import com.example.forgetpass.domain.AuthenticationAuditLog;
import com.example.forgetpass.domain.Patient;
import com.example.forgetpass.repositories.AuthenticationAuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuthenticationAuditLogRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditService(AuthenticationAuditLogRepository repo) {
        this.repo = repo;
    }

    public void log(String eventType, Patient patient, String ip, String userAgent, boolean success, Map<String, Object> metadata) {
        AuthenticationAuditLog entry = new AuthenticationAuditLog();
        entry.setEventType(eventType);
        entry.setPatient(patient);
        entry.setIpAddress(ip);
        entry.setUserAgent(userAgent);
        entry.setSuccess(success);
        try {
            entry.setMetadataJson(metadata != null ? objectMapper.writeValueAsString(metadata) : null);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit metadata: {}", e.getMessage());
        }
        repo.save(entry);
    }
}
