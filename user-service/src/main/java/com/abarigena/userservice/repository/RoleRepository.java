package com.abarigena.userservice.repository;

import com.abarigena.userservice.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * Используется для поиска ролей по имени.
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    /**
     * Находит роль по ее имени.
     *
     * @param name имя роли.
     * @return {@link Optional} роли.
     */
    Optional<Role> findByName(Role.RoleType name);
}