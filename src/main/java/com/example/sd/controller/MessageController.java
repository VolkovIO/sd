package com.example.sd.controller;

import com.example.sd.dto.SendMessageRequest;
import com.example.sd.entity.Message;
import com.example.sd.entity.User;
import com.example.sd.repository.UserRepository;
import com.example.sd.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    @GetMapping
    public List<Message> getChatMessages(@PathVariable Long chatId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromDetails(userDetails);
        return messageService.getChatMessages(chatId, currentUser);
    }

    @GetMapping("/page")
    public Page<Message> getChatMessagesPaginated(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = getUserFromDetails(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return messageService.getChatMessages(chatId, currentUser, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(@PathVariable Long chatId,
                               @RequestBody SendMessageRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = getUserFromDetails(userDetails);
        return messageService.sendMessage(chatId, currentUser, request.getText());
    }

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
