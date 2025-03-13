package com.abarigena.userservice.service;

import com.abarigena.dto.UserDto;
import com.abarigena.dto.UserInfoDto;
import com.abarigena.userservice.entity.Role;
import com.abarigena.userservice.entity.User;
import com.abarigena.userservice.repository.RoleRepository;
import com.abarigena.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями.
 * Включает методы для создания пользователей, поиска по email, назначения ролей и получения информации о пользователях.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Инициализирует роли по умолчанию, если они еще не созданы.
     * Роли администратора и пользователя создаются при запуске приложения.
     */
    @PostConstruct
    public void initRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleType.ROLE_ADMIN);

            Role userRole = new Role();
            userRole.setName(Role.RoleType.ROLE_USER);

            roleRepository.save(adminRole);
            roleRepository.save(userRole);
        }
    }

    /**
     * Создает нового пользователя на основе переданных данных.
     *
     * @param userDto данные для создания нового пользователя.
     * @return {@link UserDto} созданного пользователя.
     * @throws RuntimeException если email или username уже заняты.
     */
    @Transactional
    public UserDto save(UserDto userDto) {
        // Проверка существования пользователя
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Создание нового пользователя
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setUsername(userDto.getUsername());

        // По умолчанию назначаем роль пользователя
        Role userRole = roleRepository.findByName(Role.RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Collections.singleton(userRole));

        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

    /**
     * Находит пользователя по его email.
     *
     * @param email email пользователя.
     * @return {@link UserDto} найденного пользователя.
     * @throws RuntimeException если пользователь не найден.
     */
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDto(user);
    }

    /**
     * Преобразует {@link User} в {@link UserDto}.
     *
     * @param user пользователь, который будет преобразован.
     * @return {@link UserDto} с данными пользователя.
     */
    private UserDto convertToDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getUsername())
                .roles(roles)
                .build();
    }

    /**
     * Назначает роль администратора пользователю по email.
     *
     * @param email email пользователя, которому будет назначена роль администратора.
     * @throws RuntimeException если пользователь или роль администратора не найдены.
     */
    @Transactional
    public void assignAdminRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role adminRole = roleRepository.findByName(Role.RoleType.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(adminRole);
        user.setRoles(roles);

        userRepository.save(user);
    }

    /**
     * Находит информацию о пользователе по его ID.
     *
     * @param userId ID пользователя.
     * @return {@link UserInfoDto} с информацией о пользователе.
     * @throws UsernameNotFoundException если пользователь с данным ID не найден.
     */
    public UserInfoDto findById(String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с ID: " + userId));

        return new UserInfoDto(String.valueOf(user.getId()), user.getUsername(), user.getEmail());
    }

    /**
     * Возвращает текущего пользователя по его ID.
     *
     * @param userId ID пользователя.
     * @return {@link UserDto} с данными текущего пользователя.
     * @throws UsernameNotFoundException если пользователь не найден.
     */
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с ID: " + userId));
        return convertToDto(user);
    }
}
