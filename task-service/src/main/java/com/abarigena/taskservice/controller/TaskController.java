package com.abarigena.taskservice.controller;


import com.abarigena.taskservice.dto.*;
import com.abarigena.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Задачи", description = "API для управления задачами")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Получить все задачи с фильтрацией")
    public ResponseEntity<Page<TaskDto>> getTasks(TaskFilterRequest filterRequest) {
        Page<TaskDto> tasks = taskService.findTasks(filterRequest);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получить задачу по ID")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
        TaskDto task = taskService.findTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/create")
    @Operation(summary = "Создать новую задачу")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDto createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Обновить существующую задачу")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request) {
        TaskDto updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Удалить задачу")
    @PreAuthorize("hasRole('ADMIN') or @taskService.findTaskById(#taskId).authorId == authentication.principal")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/comments")
    @Operation(summary = "Добавить комментарий к задаче")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDto comment = taskService.addComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/{taskId}/comments")
    @Operation(summary = "Получить все комментарии для задачи")
    public ResponseEntity<List<CommentDto>> getTaskComments(@PathVariable Long taskId) {
        List<CommentDto> comments = taskService.getTaskComments(taskId);
        return ResponseEntity.ok(comments);
    }
}
