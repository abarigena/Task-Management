package com.abarigena.userservice.entity;

import jakarta.persistence.*;

/**
 * Класс, представляющий роль пользователя в системе.
 * Роли могут быть использованы для определения прав доступа.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название роли, которое может быть одним из значений из перечисления {@link RoleType}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    public enum RoleType {
        ROLE_ADMIN,
        ROLE_USER
    }

    // Конструкторы
    public Role() {
    }

    public Role(Long id, RoleType name) {
        this.id = id;
        this.name = name;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleType getName() {
        return name;
    }

    public void setName(RoleType name) {
        this.name = name;
    }
}