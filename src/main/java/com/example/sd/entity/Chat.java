package com.example.sd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Добавить уникальное ограничение
// При попытке сохранить дубликат будет выброшено исключение DataIntegrityViolationException
@Table(name = "chat",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_advert_initiator_recipient",
                columnNames = {"advert_id", "initiator_id", "recipient_id"}
        ))
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advert_id", nullable = false)
    private Advert advert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private Instant createdAt;

}
