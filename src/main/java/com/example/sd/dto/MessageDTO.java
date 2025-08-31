package com.example.sd.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MessageDTO {
    private Long id;
    private String text;
    private Instant timestamp;
    private UserDTO sender;
    private ChatDTO chat;

    @Data
    public static class UserDTO {
        private Long id;
        private String username;
    }

    @Data
    public static class ChatDTO {
        private Long id;
    }
}
