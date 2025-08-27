package com.example.sd.repository;

import com.example.sd.model.entity.Advert;
import com.example.sd.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
Эта аннотация - ключевая. Она говорит Spring: "Запусти тест только для JPA слоя!"
Аннотация @DataJpaTest по умолчанию автоматически:
-Настраивает in-memory БД (H2).
-Создает все таблицы на основе наших Entity-классов.
-Настраивает JPA, Hibernate.
-Подгружает только JPA-компоненты, а не весь контекст приложения (тесты работают быстрее).
 */
@DataJpaTest
class AdvertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdvertRepository advertRepository;

    @Test
    void testFindById_WhenAdvertExists_ShouldReturnAdvertWithAuthor() {
        // 1. GIVEN - Подготовка данных: нужно создать и пользователя, и объявление
        // Создаем и сохраняем пользователя
        User author = new User();
        author.setUsername("authorUser");
        author.setEmail("author@mail.com");
        author.setPasswordHash("hash");
        author.setCreatedAt(Instant.now());
        entityManager.persistAndFlush(author); // Сначала сохраняем автора

        // Создаем объявление, которое ссылается на сохраненного пользователя
        Advert expectedAdvert = new Advert();
        expectedAdvert.setTitle("Test Title");
        expectedAdvert.setDescription("Test Description");
        expectedAdvert.setAuthor(author); // Устанавливаем связь!
        expectedAdvert.setCreatedAt(Instant.now());

        // Сохраняем объявление
        entityManager.persistAndFlush(expectedAdvert);

        // 2. WHEN - Ищем объявление по его ID
        // findById возвращает Optional<Advert>
        Optional<Advert> foundAdvert = advertRepository.findById(expectedAdvert.getId());

        // 3. THEN - Проверяем результат
        assertThat(foundAdvert).isPresent();
        // Проверяем основные поля
        assertThat(foundAdvert.get().getTitle()).isEqualTo("Test Title");
        // Самое главное: проверяем, что связь с User работает и автор не NULL
        assertThat(foundAdvert.get().getAuthor()).isNotNull();
        // Проверяем данные автора (благодаря связи ManyToOne и fetch = LAZY/ EAGER)
        assertThat(foundAdvert.get().getAuthor().getUsername()).isEqualTo("authorUser");
    }

}