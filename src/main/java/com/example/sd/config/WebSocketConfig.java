package com.example.sd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Настраиваем брокер сообщений для рассылки сообщений клиентам
        config.enableSimpleBroker("/topic");

        // Префикс для сообщений, которые направляются к @MessageMapping методам
        config.setApplicationDestinationPrefixes("/app");

        // Префикс для пользовательских точек назначения (optional)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрируем endpoint для подключения WebSocket клиентов
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Разрешаем все origins (для разработки)
                .withSockJS(); // Включаем поддержку SockJS для fallback
    }
}
