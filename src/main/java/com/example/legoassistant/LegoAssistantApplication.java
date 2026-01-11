package com.example.legoassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LegoAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegoAssistantApplication.class, args);
        System.out.println("🚀 LEGO Assistant is running! Go to: http://localhost:8080");
    }
}