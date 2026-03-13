package com.permguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PermguardApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermguardApplication.class, args);
    }
}