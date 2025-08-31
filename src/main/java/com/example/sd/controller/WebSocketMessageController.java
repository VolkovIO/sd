package com.example.sd.controller;

import com.example.sd.dto.MessageDTO;
import com.example.sd.entity.Message;
import com.example.sd.entity.User;
import com.example.sd.repository.UserRepository;
import com.example.sd.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    @MessageMapping("/ping")
    public void handlePing() {
        // Просто отвечаем на ping, ничего не делаем
        log.debug("Ping received");
    }

    @MessageMapping("/chat/{chatId}/send")
    @SendTo("/topic/chat.{chatId}")
    public MessageDTO sendMessage(@DestinationVariable Long chatId,
                                  Message message,
                                  Principal principal) {

        try {
            User sender = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

//            User sender = (User) ((Authentication) principal).getPrincipal();

            log.info("Received message for chat {} from user {}: {}", chatId, principal.getName(), message.getText());

            // Сохраняем сообщение в БД
            Message savedMessage = messageService.sendMessage(chatId, sender, message.getText());

            // Возвращаем сохраненное сообщение для рассылки подписчикам
            return messageService.convertToDTO(savedMessage);

        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            throw new RuntimeException("Failed to process message", e);
        }
    }

    @MessageMapping("/chat/typing")
    @SendToUser("/queue/typing")
    public String handleTyping(String typingEvent, Principal principal) {
        log.info("User {} is typing: {}", principal.getName(), typingEvent);
        return typingEvent;
    }
}
