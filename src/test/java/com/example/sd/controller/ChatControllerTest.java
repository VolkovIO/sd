package com.example.sd.controller;

import com.example.sd.entity.Advert;
import com.example.sd.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdvertRepository advertRepository;

    private User testUser1, testUser2;
    private Advert testAdvert;

    @BeforeEach
    void setUp() {
        // Очистка и создание тестовых данных
        userRepository.deleteAll();
        advertRepository.deleteAll();

        testUser1 = createUser("testuser1", "test1@example.com");
        testUser2 = createUser("testuser2", "test2@example.com");
        testAdvert = createAdvert("Test Advert", testUser2);
    }

    @Test
    @WithMockUser(username = "testuser1")
    void getMyChats_WithAuth_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/chats/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "testuser1")
    void createChat_WithAuth_ShouldReturnCreated() throws Exception {
        String requestJson = """
            {
                "advertId": %d,
                "recipientId": %d
            }
            """.formatted(testAdvert.getId(), testUser2.getId());

        mockMvc.perform(post("/api/chats")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.advert.id").value(testAdvert.getId()))
                .andExpect(jsonPath("$.initiator.username").value("testuser1"))
                .andExpect(jsonPath("$.recipient.username").value("testuser2"));
    }

    @Test
    void createChat_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String requestJson = """
            {
                "advertId": 1,
                "recipientId": 2
            }
            """;

        mockMvc.perform(post("/api/chats")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    // Вспомогательные методы
    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    private Advert createAdvert(String title, User author) {
        Advert advert = new Advert();
        advert.setTitle(title);
        advert.setDescription("Test Description");
        advert.setAuthor(author);
        advert.setCreatedAt(Instant.now());
        return advertRepository.save(advert);
    }
}