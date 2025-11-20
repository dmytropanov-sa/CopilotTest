package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "forgetpass.cleanup.cron=0 5 4 * * *")
public class TokenCleanupServiceConfigTest {

    @Autowired
    private TokenCleanupService tokenCleanupService;

    @Test
    void serviceShouldBindConfiguredCron() {
        assertThat(tokenCleanupService).isNotNull();
        assertThat(tokenCleanupService.getCleanupCron()).isEqualTo("0 5 4 * * *");
    }
}
