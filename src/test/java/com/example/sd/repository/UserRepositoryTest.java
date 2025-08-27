package com.example.sd.repository;

import com.example.sd.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // Потрясающая библиотека для удобных проверок

/*
Эта аннотация - ключевая. Она говорит Spring: "Запусти тест только для JPA слоя!"
Аннотация @DataJpaTest по умолчанию автоматически:
-Настраивает in-memory БД (H2).
-Создает все таблицы на основе наших Entity-классов.
-Настраивает JPA, Hibernate.
-Подгружает только JPA-компоненты, а не весь контекст приложения (тесты работают быстрее).
 */
@DataJpaTest
class UserRepositoryTest {

    // TestEntityManager - это помощник для работы с сущностями в тестах.
    // Аналог стандартного EntityManager, но с дополнительными удобными методами для тестирования.
    @Autowired
    private TestEntityManager entityManager;

    // Наш главный объект тестирования
    @Autowired
    private UserRepository userRepository;

    // Метод, который проверяет успешное сохранение и поиск пользователя по имени
    @Test
    void testFindByUsername_WhenUserExists_ShouldReturnUser() {
        // 1. GIVEN (Дано) - Подготовка данных
        // Создаем нового пользователя и сохраняем его в БД с помощью TestEntityManager
        String username = "testUser";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setEmail("test@mail.com");
        expectedUser.setPasswordHash("secretHash");
        expectedUser.setCreatedAt(Instant.now());

        // Сохраняем сущность в БД. Метод persist() сохраняет, а flush() немедленно применяет изменения.
        entityManager.persistAndFlush(expectedUser);

        // 2. WHEN (Когда) - Выполнение действия, которое тестируем
        // Ищем пользователя по имени через наш репозиторий
        Optional<User> foundUser = userRepository.findByUsername(username);

        // 3. THEN (Тогда) - Проверка результата
        // Убеждаемся, что пользователь найден
        assertThat(foundUser).isPresent();
        // Убеждаемся, что найден именно тот пользователь (проверяем по имени)
        assertThat(foundUser.get().getUsername()).isEqualTo(username);
        // Убеждаемся, что у найденного пользователя проставлен ID (значит, он был сохранен)
        assertThat(foundUser.get().getId()).isNotNull();
    }

    @Test
    void testFindByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        // 1. GIVEN - База пустая, мы ничего не сохраняли

        // 2. WHEN - Пытаемся найти несуществующего пользователя
        Optional<User> foundUser = userRepository.findByUsername("nonExistentUser");

        // 3. THEN - Убеждаемся, что результат пустой
        assertThat(foundUser).isEmpty();
    }

}