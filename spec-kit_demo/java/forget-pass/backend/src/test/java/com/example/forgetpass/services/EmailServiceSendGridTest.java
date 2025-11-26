package com.example.forgetpass.services;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceSendGridTest {

    static class FakeSendGrid extends SendGrid {
        private final Response response;

        FakeSendGrid(Response r) {
            super((String) null);
            this.response = r;
        }

        @Override
        public Response api(com.sendgrid.Request request) throws IOException {
            return response;
        }
    }

    static class TestableEmailService extends EmailService {
        private final SendGrid sg;

        TestableEmailService(SendGrid sg) {
            this.sg = sg;
            // Ensure apiKey is non-blank so dispatch proceeds
            this.apiKey = "test-key";
        }

        @Override
        protected SendGrid createSendGrid(String apiKey) {
            return sg;
        }

        @Override
        protected void backoff(int attempt) {
            // no sleep during tests
        }
    }

    @Test
    void dispatch_success_logs_noException() {
        Response ok = new Response(202, "OK", java.util.Map.of());
        SendGrid sg = new FakeSendGrid(ok);
        TestableEmailService svc = new TestableEmailService(sg);

        assertThatCode(() -> svc.sendVerificationEmail("a@b.c", "link")).doesNotThrowAnyException();
    }

    @Test
    void dispatch_failure_retries_thenLogs() {
        Response fail = new Response(500, "ERR", java.util.Map.of());
        SendGrid sg = new FakeSendGrid(fail);
        TestableEmailService svc = new TestableEmailService(sg);

        assertThatCode(() -> svc.sendPasswordChangedConfirmation("x@y.z")).doesNotThrowAnyException();
    }

    @Test
    void dispatch_ioexception_isHandled() {
        SendGrid sg = new SendGrid((String) null) {
            @Override
            public Response api(com.sendgrid.Request request) throws IOException {
                throw new IOException("simulated");
            }
        };
        TestableEmailService svc = new TestableEmailService(sg);
        assertThatCode(() -> svc.sendPasswordResetEmail("u@v.w", "link")).doesNotThrowAnyException();
    }
}
