package com.example.sd.security;

import com.example.sd.model.entity.Advert;
import com.example.sd.model.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdvertControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Advert testAdvert;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setCreatedAt(Instant.now());
        testUser = userRepository.save(testUser);

        // Создаем тестовое объявление
        testAdvert = new Advert();
        testAdvert.setTitle("Test Advert");
        testAdvert.setDescription("Test Description");
        testAdvert.setAuthor(testUser);
        testAdvert.setCreatedAt(Instant.now());
        testAdvert = advertRepository.save(testAdvert);
    }

    @Test
    void getAdverts_WithoutAuth_ShouldReturnOk() throws Exception {
        // GET /api/adverts - ДОЛЖЕН проходить (permitAll)
        mockMvc.perform(get("/api/adverts"))
                .andExpect(status().isOk());
    }

    @Test
    void getAdvertById_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // GET /api/adverts/1 - ДОЛЖЕН возвращать 401 Unauthorized
        mockMvc.perform(get("/api/adverts/" + testAdvert.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAdvert_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // POST /api/adverts - ДОЛЖЕН возвращать 401 Unauthorized
        mockMvc.perform(post("/api/adverts")
                        .contentType("application/json")
                        .content("{\"title\": \"New Advert\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAdvertById_WithAuth_ShouldReturnOk() throws Exception {
        // GET /api/adverts/1 - с аутентификацией ДОЛЖЕН проходить
        mockMvc.perform(get("/api/adverts/" + testAdvert.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createAdvert_WithAuth_ShouldReturnOk() throws Exception {
        // POST /api/adverts - с аутентификацией ДОЛЖЕН проходить
        mockMvc.perform(post("/api/adverts")
                        .contentType("application/json")
                        .content("{\"title\": \"New Advert\"}"))
                .andExpect(status().isOk());
    }

}
