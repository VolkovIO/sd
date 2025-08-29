package com.example.sd.service;

import com.example.sd.entity.Advert;
import com.example.sd.entity.Chat;
import com.example.sd.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private AdvertRepository advertRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void getOrCreateChat_WhenChatExists_ShouldReturnExistingChat() {
        // given
        User currentUser = createUser(1L, "user1");
        User recipient = createUser(2L, "user2");
        Advert advert = createAdvert(1L, recipient);
        Chat existingChat = createChat(advert, currentUser, recipient);

        when(advertRepository.findById(1L)).thenReturn(Optional.of(advert));
        when(chatRepository.findByAdvertIdAndInitiatorIdAndRecipientId(1L, 1L, 2L))
                .thenReturn(Optional.of(existingChat));

        // when
        Chat result = chatService.getOrCreateChat(1L, currentUser, recipient);

        // then
        assertThat(result).isEqualTo(existingChat);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void getOrCreateChat_WhenChatNotExists_ShouldCreateNewChat() {
        // given
        User currentUser = createUser(1L, "user1");
        User recipient = createUser(2L, "user2");
        Advert advert = createAdvert(1L, recipient);
        Chat newChat = createChat(advert, currentUser, recipient);

        when(advertRepository.findById(1L)).thenReturn(Optional.of(advert));
        when(chatRepository.findByAdvertIdAndInitiatorIdAndRecipientId(1L, 1L, 2L))
                .thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenReturn(newChat);

        // when
        Chat result = chatService.getOrCreateChat(1L, currentUser, recipient);

        // then
        assertThat(result).isEqualTo(newChat);
        verify(chatRepository).save(any());
    }

    @Test
    void getOrCreateChat_WhenSelfChat_ShouldThrowException() {
        // given
        User user = createUser(1L, "user1");
        Advert advert = createAdvert(1L, user);

        when(advertRepository.findById(1L)).thenReturn(Optional.of(advert));

        // when & then
        assertThatThrownBy(() -> chatService.getOrCreateChat(1L, user, user))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot create chat with yourself");
    }

    // Вспомогательные методы
    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Advert createAdvert(Long id, User author) {
        Advert advert = new Advert();
        advert.setId(id);
        advert.setAuthor(author);
        return advert;
    }

    private Chat createChat(Advert advert, User initiator, User recipient) {
        return Chat.builder()
                .advert(advert)
                .initiator(initiator)
                .recipient(recipient)
                .build();
    }
}