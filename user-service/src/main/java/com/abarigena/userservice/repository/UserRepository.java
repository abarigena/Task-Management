package com.abarigena.userservice.repository;

import com.abarigena.userservice.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Используется для поиска пользователей по email и username.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Находит пользователя по его email.
     *
     * @param email email пользователя.
     * @return {@link Optional} пользователя.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя с таким email.
     *
     * @param email email пользователя.
     * @return {@code true}, если пользователь существует, иначе {@code false}.
     */
    Boolean existsByEmail(String email);

    /**
     * Проверяет существование пользователя с таким username.
     *
     * @param username имя пользователя.
     * @return {@code true}, если пользователь существует, иначе {@code false}.
     */
    Boolean existsByUsername(String username);
}
