package com.abarigena.userservice;

import com.abarigena.dto.UserDto;
import com.abarigena.dto.UserInfoDto;
import com.abarigena.userservice.entity.Role;
import com.abarigena.userservice.entity.User;
import com.abarigena.userservice.repository.RoleRepository;
import com.abarigena.userservice.repository.UserRepository;
import com.abarigena.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private UserDto userDto;
    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");
        userDto.setUsername("testuser");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setUsername("testuser");

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleType.ROLE_USER);

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleType.ROLE_ADMIN);
    }

    @Test
    public void testSaveUser() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            savedUser.setRoles(Collections.singleton(userRole));
            return savedUser;
        });

        UserDto savedUser = userService.save(userDto);

        assertNotNull(savedUser);
        assertEquals(userDto.getEmail(), savedUser.getEmail());
        assertEquals(userDto.getUsername(), savedUser.getUsername());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(roleRepository, times(1)).findByName(Role.RoleType.ROLE_USER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testSaveUserWithExistingEmail() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.save(userDto);
        });

        assertEquals("Email is already in use!", exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testSaveUserWithExistingUsername() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.save(userDto);
        });

        assertEquals("Username is already taken!", exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testFindByEmail() {
        user.setRoles(Collections.singleton(userRole));
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        UserDto foundUser = userService.findByEmail(userDto.getEmail());

        assertNotNull(foundUser);
        assertEquals(userDto.getEmail(), foundUser.getEmail());
        assertEquals(userDto.getUsername(), foundUser.getUsername());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
    }

    @Test
    public void testFindByEmailNotFound() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.findByEmail(userDto.getEmail());
        });

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
    }

    @Test
    public void testAssignAdminRole() {
        user.setRoles(new HashSet<>());

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(Role.RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.assignAdminRole(userDto.getEmail());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(roleRepository, times(1)).findByName(Role.RoleType.ROLE_ADMIN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testAssignAdminRoleUserNotFound() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.assignAdminRole(userDto.getEmail());
        });

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAssignAdminRoleAdminRoleNotFound() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(Role.RoleType.ROLE_ADMIN)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.assignAdminRole(userDto.getEmail());
        });

        assertEquals("Admin role not found", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(roleRepository, times(1)).findByName(Role.RoleType.ROLE_ADMIN);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAssignAdminRoleWithExistingRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));
        when(roleRepository.findByName(Role.RoleType.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.assignAdminRole(userDto.getEmail());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(roleRepository, times(1)).findByName(Role.RoleType.ROLE_ADMIN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserInfoDto userInfo = userService.findById("1");

        assertNotNull(userInfo);
        assertEquals(user.getId().toString(), userInfo.getId());
        assertEquals(user.getUsername(), userInfo.getUsername());
        assertEquals(user.getEmail(), userInfo.getEmail());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.findById("1");
        });

        assertEquals("Пользователь не найден с ID: 1", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetCurrentUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto currentUser = userService.getCurrentUser(1L);

        assertNotNull(currentUser);
        assertEquals(user.getId().toString(), currentUser.getId());
        assertEquals(user.getEmail(), currentUser.getEmail());
        assertEquals(user.getUsername(), currentUser.getUsername());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetCurrentUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getCurrentUser(1L);
        });

        assertEquals("Пользователь не найден с ID: 1", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }
}
