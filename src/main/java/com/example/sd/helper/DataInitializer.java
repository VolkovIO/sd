package com.example.sd.helper;

import com.example.sd.entity.Advert;
import com.example.sd.entity.Chat;
import com.example.sd.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.ChatRepository;
import com.example.sd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdvertRepository advertRepository;
    private final ChatRepository chatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Создаем тестовых пользователей
        createTestUserIfNotExists("testuser1", "test1@example.com", "password123");
        createTestUserIfNotExists("testuser2", "test2@example.com", "password123");

        // Создаем тестовое объявление
        User user2 = userRepository.findByUsername("testuser2")
                .orElseThrow(() -> new RuntimeException("Test user2 not found"));
        Advert advert = createAdvertIfNotExists("Test Advert", "Test description", user2);

        // Создаем тестовый чат
        User user1 = userRepository.findByUsername("testuser1")
                .orElseThrow(() -> new RuntimeException("Test user1 not found"));
        createTestChatIfNotExists(advert, user1, user2);
    }

    private void createTestUserIfNotExists(String username, String email, String plainPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.info("Username {} already exists, skipping creation", username);
            return;
        }

        try {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(plainPassword))
                    .createdAt(Instant.now())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Created test user: {} with ID: {}", username, savedUser.getId());

        } catch (Exception e) {
            log.error("Failed to create test user {}: {}", username, e.getMessage());
        }
    }

    private Advert createAdvertIfNotExists(String title, String description, User author) {
        return advertRepository.findByTitle(title)
                .orElseGet(() -> {
                    Advert advert = Advert.builder()
                            .title(title)
                            .description(description)
                            .author(author)
                            .createdAt(Instant.now())
                            .build();
                    Advert savedAdvert = advertRepository.save(advert);
                    log.info("Created test advert: {} with ID: {}", title, savedAdvert.getId());
                    return savedAdvert;
                });
    }

    private void createTestChatIfNotExists(Advert advert, User initiator, User recipient) {
        boolean chatExists = chatRepository.findByAdvertIdAndInitiatorIdAndRecipientId(
                advert.getId(), initiator.getId(), recipient.getId()).isPresent();

        if (!chatExists) {
            Chat chat = Chat.builder()
                    .advert(advert)
                    .initiator(initiator)
                    .recipient(recipient)
                    .createdAt(Instant.now())
                    .build();
            Chat savedChat = chatRepository.save(chat);
            log.info("Created test chat between {} and {} for advert '{}' with ID: {}",
                    initiator.getUsername(), recipient.getUsername(), advert.getTitle(), savedChat.getId());
        } else {
            log.info("Test chat already exists between {} and {} for advert '{}'",
                    initiator.getUsername(), recipient.getUsername(), advert.getTitle());
        }
    }
}
