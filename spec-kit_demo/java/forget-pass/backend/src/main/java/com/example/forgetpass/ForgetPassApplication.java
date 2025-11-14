package com.example.forgetpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ForgetPassApplication {
    static {
        // Ensure SQLite data directory exists before JPA/Hibernate tries to open the DB
        String baseDir = System.getProperty("user.dir");
        java.io.File dataDir = new java.io.File(baseDir, "data");
        if (!dataDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dataDir.mkdirs();
        }
    }
    public static void main(String[] args) {
        SpringApplication.run(ForgetPassApplication.class, args);
    }
}
