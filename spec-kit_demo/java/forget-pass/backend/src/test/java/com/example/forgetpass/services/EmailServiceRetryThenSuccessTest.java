package com.example.forgetpass.services;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceRetryThenSuccessTest {

    static class CyclingSendGrid extends SendGrid {
        private final Response[] responses;
        private int idx = 0;

        CyclingSendGrid(Response... responses) {
            super((String) null);
            this.responses = responses;
        }

        @Override
        public Response api(com.sendgrid.Request request) {
            Response r = responses[Math.min(idx, responses.length - 1)];
            idx++;
            return r;
        }
    }

    static class TestableEmailService extends EmailService {
        private final SendGrid sg;

        TestableEmailService(SendGrid sg) {
            this.sg = sg;
            this.apiKey = "k"; // ensure non-empty
            this.maxAttempts = 4;
        }

        @Override
        protected SendGrid createSendGrid(String apiKey) {
            return sg;
        }

        @Override
        protected void backoff(int attempt) {
            // avoid sleeping in tests
        }
    }

    @Test
    void dispatch_retries_then_succeeds() {
        Response fail = new Response(500, "err", java.util.Map.of());
        Response ok = new Response(202, "OK", java.util.Map.of());
        SendGrid sg = new CyclingSendGrid(fail, fail, ok);
        TestableEmailService svc = new TestableEmailService(sg);

        assertThatCode(() -> svc.sendVerificationEmail("a@b.c", "link")).doesNotThrowAnyException();
    }
}
