package com.example.legoassistant;

import com.example.legoassistant.model.User;
import com.example.legoassistant.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class LegoAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegoAssistantApplication.class, args);
        System.out.println("🚀 LEGO Assistant is running! Go to: http://localhost:8080");
    }

     @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                userRepository.save(admin);
                System.out.println("✅ Default user creat automat: admin / admin123");
            }
        };
    }
}