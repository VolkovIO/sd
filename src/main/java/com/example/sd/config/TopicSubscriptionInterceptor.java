package com.example.sd.config;

import com.example.sd.entity.User;
import com.example.sd.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicSubscriptionInterceptor implements ChannelInterceptor {

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication authentication = (Authentication) accessor.getUser();
            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith("/topic/chat.")) {
                // Проверяем доступ к чату
                try {
                    Long chatId = extractChatIdFromDestination(destination);

                    if (authentication == null) {
                        throw new SecurityException("Not authenticated");
                    }

                    User user = (User) authentication.getPrincipal();
                    chatService.getChatById(chatId, user); // Выбросит исключение если нет доступа

                    log.info("User {} subscribed to chat {}", user.getUsername(), chatId);

                } catch (Exception e) {
                    log.error("Subscription denied to {}: {}", destination, e.getMessage());
                    return null; // Отклоняем подписку
                }
            }
        }
        return message;
    }

    private Long extractChatIdFromDestination(String destination) {
        try {
            String[] parts = destination.split("\\.");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid destination format: " + destination);
        }
    }
}
