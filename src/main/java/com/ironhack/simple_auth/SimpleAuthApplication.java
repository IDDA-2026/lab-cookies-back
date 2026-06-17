package com.example.simpleauth;

import com.example.simpleauth.model.User;
import com.example.simpleauth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SimpleAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleAuthApplication.class, args);
    }

 
    @Bean
    CommandLineRunner seedDemoUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("demo@ironhack.com").isEmpty()) {
                User demo = new User();
                demo.setEmail("demo@ironhack.com");
                demo.setPassword(passwordEncoder.encode("password"));
                demo.setName("Demo User");
                userRepository.save(demo);
                System.out.println("✅  Demo user created: demo@ironhack.com / password");
            }
        };
    }
}