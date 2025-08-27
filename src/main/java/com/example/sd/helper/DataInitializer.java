package com.example.sd.helper;

import com.example.sd.model.entity.User;
import com.example.sd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User user = User.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .createdAt(Instant.now())
                    .build();
            userRepository.save(user);
        }
    }
}
