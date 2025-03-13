package com.abarigena.taskservice;

import com.abarigena.dto.exception.ResourceNotFoundException;
import com.abarigena.dto.exception.UnauthorizedException;
import com.abarigena.taskservice.client.UserServiceClient;
import com.abarigena.taskservice.dto.*;
import com.abarigena.taskservice.entity.Comment;
import com.abarigena.taskservice.entity.Task;
import com.abarigena.taskservice.repository.CommentRepository;
import com.abarigena.taskservice.repository.TaskRepository;
import com.abarigena.taskservice.security.SecurityUtils;
import com.abarigena.taskservice.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestTaskService {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.TaskStatus.PENDING);
        task.setPriority(Task.TaskPriority.MEDIUM);
        task.setAuthorId("user1");
    }

    @Test
    void findTaskById_ShouldReturnTaskDto_WhenTaskExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDto result = taskService.findTaskById(1L);

        assertEquals(task.getId(), result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getPriority(), result.getPriority());
        assertEquals(task.getAuthorId(), result.getAuthorId());

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void findTaskById_ShouldThrowResourceNotFoundException_WhenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.findTaskById(1L));

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void createTask_ShouldReturnTaskDto_WhenTaskIsCreated() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setPriority(Task.TaskPriority.HIGH);
        request.setAssigneeIds(Set.of("user2", "user3"));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("user1");

            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task savedTask = invocation.getArgument(0);
                savedTask.setId(2L);
                return savedTask;
            });

            when(userServiceClient.getUserInfo("user2")).thenReturn(new UserInfoDto("user2", "User2", "user2@example.com"));
            when(userServiceClient.getUserInfo("user3")).thenReturn(new UserInfoDto("user3", "User3", "user3@example.com"));

            TaskDto result = taskService.createTask(request);

            assertEquals(2L, result.getId());
            assertEquals(request.getTitle(), result.getTitle());
            assertEquals(request.getDescription(), result.getDescription());
            assertEquals(Task.TaskStatus.PENDING, result.getStatus());
            assertEquals(request.getPriority(), result.getPriority());
            assertEquals("user1", result.getAuthorId());
            assertEquals(request.getAssigneeIds(), result.getAssigneeIds());

            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    @Test
    void updateTask_ShouldUpdateTask_WhenUserIsAuthor() {

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Description");
        request.setStatus(Task.TaskStatus.IN_PROGRESS);
        request.setPriority(Task.TaskPriority.LOW);
        request.setAssigneeIds(Set.of("user4"));

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.TaskStatus.PENDING);
        task.setPriority(Task.TaskPriority.MEDIUM);
        task.setAuthorId("user1");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("user1");

            TaskDto result = taskService.updateTask(1L, request);

            assertEquals(request.getTitle(), result.getTitle());
            assertEquals(request.getDescription(), result.getDescription());
            assertEquals(request.getStatus(), result.getStatus());
            assertEquals(request.getPriority(), result.getPriority());
            assertEquals(request.getAssigneeIds(), result.getAssigneeIds());

            verify(taskRepository, times(1)).findById(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    @Test
    void deleteTask_ShouldDeleteTask_WhenUserIsAdmin() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_ShouldThrowUnauthorizedException_WhenUserIsNotAdminOrAuthor() {
        // Устанавливаем контекст безопасности для обычного пользователя
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user2",
                null,
                Collections.emptyList() // authorities (нет роли ADMIN)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(UnauthorizedException.class, () -> taskService.deleteTask(1L));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).delete(task);
    }

    @Test
    void addComment_ShouldAddComment_WhenTaskExists() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New Comment");

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("user1");

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment savedComment = invocation.getArgument(0);
                savedComment.setId(1L);
                return savedComment;
            });

            when(userServiceClient.getUserInfo("user1")).thenReturn(new UserInfoDto("user1", "username1", "user1@example.com"));

            CommentDto result = taskService.addComment(1L, request);

            assertEquals(1L, result.getId());
            assertEquals(request.getContent(), result.getContent());
            assertEquals("user1", result.getAuthorId());
            assertEquals("username1", result.getAuthorUsername());

            verify(taskRepository, times(1)).findById(1L);
            verify(commentRepository, times(1)).save(any(Comment.class));
        }
    }
}
