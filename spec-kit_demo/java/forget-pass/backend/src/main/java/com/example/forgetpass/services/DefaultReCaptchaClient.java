package com.example.forgetpass.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Component
public class DefaultReCaptchaClient implements ReCaptchaClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultReCaptchaClient.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public DefaultReCaptchaClient() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(3000);
        rf.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(rf);
    }

    @Override
    public ReCaptchaResponse verify(String secret, String token) throws Exception {
        URI uri = new URI("https://www.google.com/recaptcha/api/siteverify");
        Map<String,String> params = Map.of("secret", secret, "response", token);
        String resp = restTemplate.postForObject(uri, params, String.class);
        ReCaptchaResponse rc = mapper.readValue(resp, ReCaptchaResponse.class);
        log.debug("reCAPTCHA response: {}", resp);
        return rc;
    }
}
