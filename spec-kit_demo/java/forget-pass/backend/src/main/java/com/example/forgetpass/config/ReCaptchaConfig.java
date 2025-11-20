package com.example.forgetpass.config;

import com.example.forgetpass.services.DefaultReCaptchaClient;
import com.example.forgetpass.services.ReCaptchaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReCaptchaConfig {

    @Bean
    public DefaultReCaptchaClient defaultReCaptchaClient() {
        return new DefaultReCaptchaClient();
    }

    @Bean
    public ReCaptchaService reCaptchaService(DefaultReCaptchaClient client) {
        return new ReCaptchaService(client);
    }
}
