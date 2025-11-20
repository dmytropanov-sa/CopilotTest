package com.example.forgetpass.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final String apiKey = System.getenv("SENDGRID_API_KEY");
    private final String fromAddress = System.getenv().getOrDefault("SENDGRID_FROM", "no-reply@forget-pass.local");
    private final int maxAttempts = 3;

    public void sendVerificationEmail(String to, String link) {
        String subject = "Verify your account";
        String body = "Please verify your account by clicking: " + link + "\nThis link expires in 24 hours.";
        dispatch(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String link) {
        String subject = "Password reset request";
        String body = "You requested a password reset. Use this link within 1 hour: " + link;
        dispatch(to, subject, body);
    }

    public void sendPasswordChangedConfirmation(String to) {
        String subject = "Your password was changed";
        String body = "This is a confirmation that your password was successfully changed. If this wasn't you, contact support immediately.";
        dispatch(to, subject, body);
    }

    private void dispatch(String to, String subject, String content) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("[EMAIL:DRY-RUN] to='{}' subject='{}' content='{}'", to, subject, abbreviate(content));
            return;
        }
        Email from = new Email(fromAddress);
        Email recipient = new Email(to);
        Content plain = new Content("text/plain", content);
        Mail mail = new Mail(from, subject, recipient, plain);

        SendGrid sg = new SendGrid(apiKey);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                int code = response.getStatusCode();
                if (code >= 200 && code < 300) {
                    log.info("Email sent: to='{}' subject='{}' status={}", to, subject, code);
                    return;
                }
                log.warn("Email send failed attempt {}: status={} body={} headers={}", attempt, code, abbreviate(response.getBody()), response.getHeaders());
            } catch (IOException ex) {
                log.warn("Email send IOException attempt {}: {}", attempt, ex.getMessage());
            }
            backoff(attempt);
        }
        log.error("Email delivery ultimately failed after {} attempts: to='{}' subject='{}'", maxAttempts, to, subject);
    }

    private void backoff(int attempt) {
        long sleepMs = Duration.ofSeconds((long) Math.pow(2, attempt)).toMillis();
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String abbreviate(String s) {
        if (s == null) return "";
        return s.length() > 180 ? s.substring(0, 177) + "..." : s;
    }
}
