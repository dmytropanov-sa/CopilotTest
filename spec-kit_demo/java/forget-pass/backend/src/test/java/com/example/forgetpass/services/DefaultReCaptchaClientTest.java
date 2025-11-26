package com.example.forgetpass.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

class DefaultReCaptchaClientTest {
    private DefaultReCaptchaClient client;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = Mockito.mock(RestTemplate.class);
        // create a subclass to inject mock RestTemplate
        client = new DefaultReCaptchaClient() {
            { 
                try {
                    java.lang.reflect.Field f = DefaultReCaptchaClient.class.getDeclaredField("restTemplate");
                    f.setAccessible(true);
                    f.set(this, restTemplate);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    void verify_parsesValidResponse() throws Exception {
        String json = "{\"success\":true,\"score\":0.9,\"action\":\"x\"}";
        when(restTemplate.postForObject(any(URI.class), anyMap(), any())).thenReturn(json);

        ReCaptchaResponse r = client.verify("s", "t");
        assertThat(r).isNotNull();
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getScore()).isEqualTo(0.9);
    }

    @Test
    void verify_throwsOnMalformedJson() throws Exception {
        when(restTemplate.postForObject(any(URI.class), anyMap(), any())).thenReturn("not-json");
        assertThrows(Exception.class, () -> client.verify("s","t"));
    }
}
