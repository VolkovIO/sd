package com.example.sd.repository;

import com.example.sd.entity.Advert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvertRepository extends JpaRepository<Advert, Long> {

    // Поиск объявления по заголовку
    Optional<Advert> findByTitle(String title);

    List<Advert> findByAuthorId(Long authorId);
}
