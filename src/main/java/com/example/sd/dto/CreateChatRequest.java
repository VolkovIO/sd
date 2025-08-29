package com.example.sd.dto;

import lombok.Data;

@Data
public class CreateChatRequest {
    private Long advertId;
    private Long recipientId;
}
