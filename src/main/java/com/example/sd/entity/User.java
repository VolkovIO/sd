package com.example.sd.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") // Меняем на "users" USER — это зарезервированное ключевое слово в SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String passwordHash;

    private Instant createdAt;

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Chat> initiatedChats;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Chat> receivedChats;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Message> sentMessages;
}
