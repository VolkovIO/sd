package com.example.sd.repository;

import com.example.sd.entity.Chat;
import com.example.sd.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Найти все сообщения в чате
    List<Message> findByChatOrderByTimestampAsc(Chat chat);

    List<Message> findByChatOrderByTimestampDesc(Chat chat);

    // Найти сообщения в чате с пагинацией
    Page<Message> findByChatOrderByTimestampDesc(Chat chat, Pageable pageable);
}
