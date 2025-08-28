package com.example.sd.repository;

import com.example.sd.model.entity.Chat;
import com.example.sd.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // Найти чат по объявлению и участникам
    Optional<Chat> findByAdvertIdAndInitiatorIdAndRecipientId(Long advertId, Long initiatorId, Long recipientId);

    // Найти все чаты пользователя (где он инициатор или получатель)
    List<Chat> findByInitiatorOrRecipient(User initiator, User recipient);

    // Найти чаты по объявлению
    List<Chat> findByAdvertId(Long advertId);
}
