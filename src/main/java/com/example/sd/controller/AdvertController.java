package com.example.sd.controller;

import com.example.sd.dto.CreateAdvertRequest;
import com.example.sd.model.entity.Advert;
import com.example.sd.model.entity.User;
import com.example.sd.repository.AdvertRepository;
import com.example.sd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/adverts")
public class AdvertController {
    private final AdvertRepository advertRepository;
    private final UserRepository userRepository;


    @GetMapping
    public List<Advert> getAdverts() {
        return advertRepository.findAll();
    }

    @GetMapping("/{id}")
    public Advert getAdvert(@PathVariable Long id) {
        return advertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Возвращаем статус 201 Created
    public Advert createAdvert(@RequestBody CreateAdvertRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {

        // Находим пользователя в БД по username из аутентификации
        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        // Создаем объявление из DTO
        Advert advert = Advert.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .createdAt(Instant.now())
                .build();

        // Сохраняем объявление
        return advertRepository.save(advert);
    }
}
