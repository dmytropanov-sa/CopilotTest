package com.example.forgetpass.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReCaptchaService {
    private static final Logger log = LoggerFactory.getLogger(ReCaptchaService.class);
    private final ReCaptchaClient client;

    public ReCaptchaService(ReCaptchaClient client) {
        this.client = client;
    }

    private String resolveSecret() {
        String prop = System.getProperty("RECAPTCHA_SECRET");
        if (System.getProperties().containsKey("RECAPTCHA_SECRET")) {
            return prop == null ? "" : prop;
        }
        String env = System.getenv("RECAPTCHA_SECRET");
        return env == null ? "" : env;
    }

    private double resolveMinScore() {
        String prop = System.getProperty("RECAPTCHA_MIN_SCORE");
        if (prop != null && !prop.isBlank()) {
            try { return Double.parseDouble(prop); } catch (NumberFormatException ignore) {}
        }
        String env = System.getenv("RECAPTCHA_MIN_SCORE");
        if (env != null && !env.isBlank()) {
            try { return Double.parseDouble(env); } catch (NumberFormatException ignore) {}
        }
        return 0.5;
    }

    public boolean validate(String token, String action) {
        String secret = resolveSecret();
        if (secret.isBlank()) {
            log.info("reCAPTCHA secret not set — skipping validation (dev)");
            return true;
        }
        try {
            ReCaptchaResponse resp = client.verify(secret, token);
            if (!resp.isSuccess()) {
                log.warn("reCAPTCHA failed: {}", resp.getErrorCodes());
                return false;
            }
            if (!action.equals(resp.getAction())) {
                log.warn("reCAPTCHA action mismatch: expected={}, got={}", action, resp.getAction());
                return false;
            }
            double minScore = resolveMinScore();
            return resp.getScore() >= minScore;
        } catch (Exception ex) {
            log.warn("reCAPTCHA verification error: {}", ex.getMessage());
            return false;
        }
    }
}
