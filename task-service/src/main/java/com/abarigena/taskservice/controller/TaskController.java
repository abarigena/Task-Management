package com.abarigena.taskservice.controller;

import com.abarigena.taskservice.dto.*;
import com.abarigena.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с задачами.
 * Предоставляет API для создания, получения, обновления и удаления задач.
 */
@RestController
@RequestMapping("/tasks")
@Tag(name = "Задачи", description = "API для управления задачами")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Получает все задачи с фильтрацией.
     *
     * @param filterRequest Параметры фильтрации задач
     * @return список задач с возможностью пагинации
     */
    @GetMapping
    @Operation(
            summary = "Получить все задачи с фильтрацией",
            description = "Возвращает список задач с возможностью фильтрации",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно получены",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
            }
    )
    public ResponseEntity<Page<TaskDto>> getTasks(@RequestBody TaskFilterRequest filterRequest) {
        Page<TaskDto> tasks = taskService.findTasks(filterRequest);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает задачу по её ID.
     *
     * @param taskId ID задачи
     * @return Задача с указанным ID
     */
    @GetMapping("/{taskId}")
    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает задачу по её уникальному идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно найдена",
                            content = @Content(schema = @Schema(implementation = TaskDto.class))),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
        TaskDto task = taskService.findTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * Получает задачи по ID автора.
     * Задачи возвращаются с пагинацией и сортировкой.
     *
     * @param authorId ID автора
     * @param page     Номер страницы
     * @param size     Количество задач на странице
     * @return Список задач автора
     */
    @GetMapping("/by-author/{authorId}")
    @Operation(
            summary = "Получить задачи по ID автора",
            description = "Возвращает список задач, созданных определённым автором, с пагинацией и сортировкой",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно получены",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Автор не найден")
            }
    )
    public ResponseEntity<Page<TaskDto>> getTasksByAuthor(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TaskDto> tasks = taskService.findTasksByAuthor(authorId, pageable);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает задачи по ID исполнителя.
     * Задачи возвращаются с пагинацией и сортировкой.
     *
     * @param assigneeId ID исполнителя
     * @param page       Номер страницы
     * @param size       Количество задач на странице
     * @return Список задач исполнителя
     */
    @GetMapping("/by-assignee/{assigneeId}")
    @Operation(
            summary = "Получить задачи по ID исполнителя",
            description = "Возвращает список задач, назначенных определённому исполнителю, с пагинацией и сортировкой",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно получены",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
            }
    )
    public ResponseEntity<Page<TaskDto>> getTasksByAssignee(
            @PathVariable String assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TaskDto> tasks = taskService.findTasksByAssignee(assigneeId, pageable);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Создаёт новую задачу на основе переданных данных.
     *
     * @param request Данные для создания задачи
     * @return Созданная задача
     */
    @PostMapping("/create")
    @Operation(
            summary = "Создать новую задачу",
            description = "Создаёт новую задачу на основе переданных данных",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача успешно создана",
                            content = @Content(schema = @Schema(implementation = TaskDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
            }
    )
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDto createdTask = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * Обновляет задачу по её ID на основе переданных данных.
     *
     * @param taskId ID задачи
     * @param request Данные для обновления задачи
     * @return Обновленная задача
     */
    @PutMapping("/{taskId}")
    @Operation(
            summary = "Обновить существующую задачу",
            description = "Обновляет задачу по её ID на основе переданных данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно обновлена",
                            content = @Content(schema = @Schema(implementation = TaskDto.class))),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
            }
    )
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request) {
        TaskDto updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Удаляет задачу по её ID.
     * Доступно только администраторам или автору задачи.
     *
     * @param taskId ID задачи для удаления
     * @return Ответ без содержимого (204)
     */
    @DeleteMapping("/{taskId}")
    @Operation(
            summary = "Удалить задачу",
            description = "Удаляет задачу по её ID. Доступно только администраторам или автору задачи.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещён"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN') or @taskService.findTaskById(#taskId).authorId == authentication.principal")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Добавляет комментарий к задаче по её ID.
     *
     * @param taskId ID задачи
     * @param request Данные комментария
     * @return Созданный комментарий
     */
    @PostMapping("/{taskId}/comments")
    @Operation(
            summary = "Добавить комментарий к задаче",
            description = "Добавляет комментарий к задаче по её ID",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий успешно добавлен",
                            content = @Content(schema = @Schema(implementation = CommentDto.class))),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
            }
    )
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDto comment = taskService.addComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Получить все комментарии для задачи по её ID.
     * Этот метод возвращает список комментариев, связанных с задачей, используя её уникальный идентификатор.
     * Если задача с указанным ID не найдена, возвращается ошибка 404.
     * @return ResponseEntity, содержащий список комментариев для задачи, если она найдена, и статус 200.
     */
    @GetMapping("/{taskId}/comments")
    @Operation(
            summary = "Получить все комментарии для задачи",
            description = "Возвращает список комментариев для задачи по её ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарии успешно получены",
                            content = @Content(schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<List<CommentDto>> getTaskComments(@PathVariable Long taskId) {
        List<CommentDto> comments = taskService.getTaskComments(taskId);
        return ResponseEntity.ok(comments);
    }
}
