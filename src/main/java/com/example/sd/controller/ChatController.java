package com.example.sd.controller;

import com.example.sd.dto.CreateChatRequest;
import com.example.sd.entity.Chat;
import com.example.sd.entity.User;
import com.example.sd.repository.ChatRepository;
import com.example.sd.service.ChatService;
import com.example.sd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @GetMapping("/api/chats/all")
    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    @GetMapping("/my")
    public List<Chat> getUserChats(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromDetails(userDetails);
        return chatService.getUserChats(currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chat createOrGetChat(@RequestBody CreateChatRequest request,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromDetails(userDetails);
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found"));

        return chatService.getOrCreateChat(request.getAdvertId(), currentUser, recipient);
    }

    @GetMapping("/{chatId}")
    public Chat getChat(@PathVariable Long chatId,
                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromDetails(userDetails);
        return chatService.getChatById(chatId, currentUser);
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
