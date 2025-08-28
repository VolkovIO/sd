package com.example.sd.repository;

import com.example.sd.model.entity.Advert;
import com.example.sd.model.entity.Chat;
import com.example.sd.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChatRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;
    private Advert advert;
    private Chat chat;

    @BeforeEach
    void setUp() {
        // Очистка базы
        messageRepository.deleteAll();
        chatRepository.deleteAll();
        advertRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых данных
        user1 = createUser("user1", "user1@example.com");
        user2 = createUser("user2", "user2@example.com");
        advert = createAdvert("Test Advert", user1);
        chat = createChat(advert, user1, user2);
    }

    @Test
    void testSaveAndFindChat() {
        // given
        Chat savedChat = chatRepository.save(chat);

        // when
        Optional<Chat> foundChat = chatRepository.findById(savedChat.getId());

        // then
        assertThat(foundChat).isPresent();
        assertThat(foundChat.get().getInitiator().getUsername()).isEqualTo("user1");
        assertThat(foundChat.get().getRecipient().getUsername()).isEqualTo("user2");
    }

    @Test
    void testFindByAdvertAndParticipants() {
        // given
        chatRepository.save(chat);

        // when
        Optional<Chat> foundChat = chatRepository.findByAdvertIdAndInitiatorIdAndRecipientId(
                advert.getId(), user1.getId(), user2.getId());

        // then
        assertThat(foundChat).isPresent();
    }

    // Вспомогательные методы
    private User createUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("hash")
                .createdAt(Instant.now())
                .build();
        return entityManager.persistAndFlush(user);
    }

    private Advert createAdvert(String title, User author) {
        Advert advert = Advert.builder()
                .title(title)
                .description("Description")
                .author(author)
                .createdAt(Instant.now())
                .build();
        return entityManager.persistAndFlush(advert);
    }

    private Chat createChat(Advert advert, User initiator, User recipient) {
        Chat chat = Chat.builder()
                .advert(advert)
                .initiator(initiator)
                .recipient(recipient)
                .createdAt(Instant.now())
                .build();
        return entityManager.persistAndFlush(chat);
    }
}