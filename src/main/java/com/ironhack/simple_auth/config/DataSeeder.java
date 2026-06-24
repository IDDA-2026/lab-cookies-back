package com.ironhack.simple_auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ironhack.simple_auth.model.User;
import com.ironhack.simple_auth.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("demo@ironhack.com", "Demo User", "USER");
        seedUser("admin@ironhack.com", "Admin User", "ADMIN");

        System.out.println(">> Default users seeded...");
    }

    private void seedUser(String email, String name, String role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User demo = new User();
        demo.setName(name);
        demo.setEmail(email);
        demo.setPassword(passwordEncoder.encode("password"));
        demo.setRole(role);

        userRepository.save(demo);
    }
}
