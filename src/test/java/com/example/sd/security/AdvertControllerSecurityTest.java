package com.example.sd.security;

import com.example.sd.entity.Advert;
import com.example.sd.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        // ОЧИСТКА БАЗЫ ПЕРЕД КАЖДЫМ ТЕСТОМ
        advertRepository.deleteAll();
        userRepository.deleteAll();

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
    @WithMockUser(username = "testuser")
    void getAdvertById_WithAuth_ShouldReturnOk() throws Exception {
        // GET /api/adverts/1 - с аутентификацией ДОЛЖЕН проходить
        mockMvc.perform(get("/api/adverts/" + testAdvert.getId()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "testuser")
    void createAdvert_WithAuth_ShouldCreateAdvert() throws Exception {
        // given
        String requestJson = """
        {
            "title": "New Test Advert",
            "description": "New Test Description"
        }
        """;

        // when & then
        mockMvc.perform(post("/api/adverts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test Advert"))
                .andExpect(jsonPath("$.author.username").value("testuser"));
    }

    @Test
    void createAdvert_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String requestJson = """
        {
            "title": "New Test Advert",
            "description": "New Test Description"
        }
        """;

        mockMvc.perform(post("/api/adverts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

}
