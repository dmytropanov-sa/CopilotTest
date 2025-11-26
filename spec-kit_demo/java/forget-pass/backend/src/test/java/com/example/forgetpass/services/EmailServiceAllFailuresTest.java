package com.example.forgetpass.services;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceAllFailuresTest {

    static class AlwaysFailSendGrid extends SendGrid {
        private final Response response;

        AlwaysFailSendGrid(Response r) {
            super((String) null);
            this.response = r;
        }

        @Override
        public Response api(com.sendgrid.Request request) {
            return response;
        }
    }

    static class TestableEmailService extends EmailService {
        private final SendGrid sg;

        TestableEmailService(SendGrid sg) {
            this.sg = sg;
            this.apiKey = "k"; // ensure non-empty so dispatch proceeds
            this.maxAttempts = 3;
        }

        @Override
        protected SendGrid createSendGrid(String apiKey) {
            return sg;
        }

        @Override
        protected void backoff(int attempt) {
            // avoid sleeping in unit tests
        }
    }

    @Test
    void dispatch_allAttemptsFail_logsAndDoesNotThrow() {
        Response fail = new Response(500, "ERR", java.util.Map.of());
        SendGrid sg = new AlwaysFailSendGrid(fail);
        TestableEmailService svc = new TestableEmailService(sg);

        assertThatCode(() -> svc.sendVerificationEmail("x@y.z", "link"))
                .doesNotThrowAnyException();
    }
}
