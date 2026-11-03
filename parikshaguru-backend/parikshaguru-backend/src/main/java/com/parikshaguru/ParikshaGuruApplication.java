package com.parikshaguru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParikshaGuruApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParikshaGuruApplication.class, args);
    }
}
