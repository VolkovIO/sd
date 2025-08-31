package com.example.sd.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService;
    private final TopicSubscriptionInterceptor topicSubscriptionInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Аутентифицируем пользователя при подключении
                    String username = accessor.getFirstNativeHeader("username");

                    if (username != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);

                            log.info("WebSocket user authenticated: {}", username);

                        } catch (Exception e) {
                            log.error("WebSocket authentication failed for user: {}", username, e);
                            return null; // Отклоняем подключение
                        }
                    } else {
                        log.warn("WebSocket connection attempt without username");
                        return null; // Отклоняем подключение без username
                    }
                }
                return message;
            }
        });
        registration.interceptors(
                new AuthenticationChannelInterceptor(userDetailsService),
                topicSubscriptionInterceptor
        );
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

    // Выносим аутентификацию в отдельный класс
    @RequiredArgsConstructor
    private static class AuthenticationChannelInterceptor implements ChannelInterceptor {

        private final UserDetailsService userDetailsService;

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                String username = accessor.getFirstNativeHeader("username");
                if (username != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                    } catch (Exception e) {
                        log.error("Authentication failed", e);
                        return null;
                    }
                }
            }
            return message;
        }
    }
}
