package com.example.sd.repository;

import com.example.sd.model.entity.Chat;
import com.example.sd.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Найти все сообщения в чате
    List<Message> findByChatOrderByTimestampAsc(Chat chat);

    List<Message> findByChatOrderByTimestampDesc(Chat chat);
}
