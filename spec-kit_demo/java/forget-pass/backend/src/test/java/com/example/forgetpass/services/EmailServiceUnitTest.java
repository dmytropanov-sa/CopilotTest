package com.example.forgetpass.services;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceUnitTest {
    @Test
    void abbreviate_handlesNullShortAndLong() throws Exception {
        EmailService svc = new EmailService();
        Method m = EmailService.class.getDeclaredMethod("abbreviate", String.class);
        m.setAccessible(true);

        assertThat((String) m.invoke(svc, (Object) null)).isEqualTo("");

        String shortVal = "short message";
        assertThat((String) m.invoke(svc, shortVal)).isEqualTo(shortVal);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('x');
        String longVal = sb.toString();
        String abbreviated = (String) m.invoke(svc, longVal);
        assertThat(abbreviated).endsWith("...")
                .hasSize(180);
    }
}
