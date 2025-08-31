package com.example.sd.service;

import com.example.sd.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageToChat(Message message) {
        try {
            String topic = "/topic/chat." + message.getChat().getId();
            messagingTemplate.convertAndSend(topic, message);

        } catch (Exception e) {
            log.error("WebSocket send failed: {}", e.getMessage());
        }
    }
}
