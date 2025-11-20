package com.example.forgetpass.services;

public interface ReCaptchaClient {
    ReCaptchaResponse verify(String secret, String token) throws Exception;
}
