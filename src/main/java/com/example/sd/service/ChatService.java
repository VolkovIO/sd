package com.example.sd.service;

import com.example.sd.entity.Advert;
import com.example.sd.entity.Chat;
import com.example.sd.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final AdvertRepository advertRepository;

    public Chat getOrCreateChat(Long advertId, User currentUser, User recipient) {
        // Проверяем существование объявления
        Advert advert = advertRepository.findById(advertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advert not found"));

        // Ищем существующий чат
        return chatRepository.findByAdvertIdAndInitiatorIdAndRecipientId(
                        advertId, currentUser.getId(), recipient.getId())
                .orElseGet(() -> createNewChat(advert, currentUser, recipient));
    }

    private Chat createNewChat(Advert advert, User initiator, User recipient) {
        // Проверка: пользователь не может создать чат с самим собой
        if (initiator.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create chat with yourself");
        }

        // Проверка: инициатор чата не может быть автором объявления
        // (чат создается когда кто-то пишет автору объявления)
        if (!advert.getAuthor().getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chat can only be created with advert author");
        }

        Chat chat = Chat.builder()
                .advert(advert)
                .initiator(initiator)
                .recipient(recipient)
                .createdAt(Instant.now())
                .build();

        return chatRepository.save(chat);
    }

    public List<Chat> getUserChats(User user) {
        return chatRepository.findByInitiatorOrRecipient(user, user);
    }

    public Chat getChatById(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found"));

        // Проверка прав доступа: пользователь должен быть участником чата
        if (!isChatParticipant(chat, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return chat;
    }

    private boolean isChatParticipant(Chat chat, User user) {
        return chat.getInitiator().getId().equals(user.getId()) ||
                chat.getRecipient().getId().equals(user.getId());
    }
}
