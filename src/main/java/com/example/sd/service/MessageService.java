package com.example.sd.service;

import com.example.sd.entity.Chat;
import com.example.sd.entity.Message;
import com.example.sd.entity.User;
import com.example.sd.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatService chatService;

    public Message sendMessage(Long chatId, User sender, String text) {
        Chat chat = chatService.getChatById(chatId, sender);

        // Проверка: отправитель должен быть участником чата
        if (!isChatParticipant(chat, sender)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a chat participant");
        }

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .text(text)
                .timestamp(Instant.now())
                .build();

        return messageRepository.save(message);
    }

    public List<Message> getChatMessages(Long chatId, User user) {
        Chat chat = chatService.getChatById(chatId, user);
        return messageRepository.findByChatOrderByTimestampAsc(chat);
    }

    public Page<Message> getChatMessages(Long chatId, User user, Pageable pageable) {
        Chat chat = chatService.getChatById(chatId, user);
        return messageRepository.findByChatOrderByTimestampDesc(chat, pageable);
    }

    private boolean isChatParticipant(Chat chat, User user) {
        return chat.getInitiator().getId().equals(user.getId()) ||
                chat.getRecipient().getId().equals(user.getId());
    }
}
