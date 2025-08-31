package com.example.sd.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ChatDTO {
    private Long id;
    private Instant createdAt;
    private AdvertDTO advert;
    private UserDTO initiator;
    private UserDTO recipient;

    @Data
    public static class AdvertDTO {
        private Long id;
        private String title;
    }

    @Data
    public static class UserDTO {
        private Long id;
        private String username;
    }
}