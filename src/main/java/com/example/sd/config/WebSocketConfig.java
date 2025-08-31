package com.example.sd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Аутентифицируем пользователя при подключении
                    String username = accessor.getFirstNativeHeader("username");
                    String password = accessor.getFirstNativeHeader("password");

                    if (username != null && password != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            // Здесь должна быть проверка пароля!
                            // Для простоты пока пропускаем проверку пароля
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);

                        } catch (Exception e) {
                            // Обработка ошибки аутентификации
                            return null; // Отклоняем сообщение
                        }
                    }
                }
                return message;
            }
        });
    }

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
