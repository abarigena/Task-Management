package com.abarigena.taskservice.service;

import com.abarigena.dto.exception.ResourceNotFoundException;
import com.abarigena.dto.exception.UnauthorizedException;
import com.abarigena.taskservice.client.UserServiceClient;
import com.abarigena.taskservice.dto.*;
import com.abarigena.taskservice.entity.Comment;
import com.abarigena.taskservice.entity.Task;
import com.abarigena.taskservice.repository.CommentRepository;
import com.abarigena.taskservice.repository.TaskRepository;
import com.abarigena.taskservice.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с задачами.
 * Предоставляет методы для создания, обновления, удаления задач, а также для работы с комментариями.
 */
@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final UserServiceClient userServiceClient;

    private final Map<String, UserInfoDto> userCache = new HashMap<>();

    @Autowired
    public TaskService(TaskRepository taskRepository, CommentRepository commentRepository,
                       UserServiceClient userServiceClient) {
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.userServiceClient = userServiceClient;
    }

    /**
     * Находит задачи по фильтрам.
     *
     * @param filterRequest запрос с фильтрами для поиска задач.
     * @return страница задач, удовлетворяющих фильтрам.
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> findTasks(TaskFilterRequest filterRequest) {

        log.debug("Фильтры: authorId={}, status={}, priority={}",
                filterRequest.getAuthorId(),
                filterRequest.getStatus(),
                filterRequest.getPriority());

        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Task> tasks = taskRepository.findByFilters(
                filterRequest.getAuthorId(),
                filterRequest.getAssigneeId(),
                filterRequest.getStatus(),
                filterRequest.getPriority(),
                pageable
        );

        return tasks.map(this::convertToTaskDto);
    }

    /**
     * Находит задачи по ID автора.
     *
     * @param authorId ID автора.
     * @param pageable параметры пагинации.
     * @return страница задач, принадлежащих автору.
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> findTasksByAuthor(String authorId, Pageable pageable) {
        log.info("Поиск задач по автору: {}", authorId);
        Page<Task> tasks = taskRepository.findByAuthorId(authorId, pageable);
        log.info("Найдено {} задач для автора {}", tasks.getTotalElements(), authorId);
        return tasks.map(this::convertToTaskDto);
    }

    /**
     * Находит задачи по ID исполнителя.
     *
     * @param assigneeId ID исполнителя.
     * @param pageable параметры пагинации.
     * @return страница задач, назначенных исполнителю.
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> findTasksByAssignee(String assigneeId, Pageable pageable) {
        log.info("Поиск задач по исполнителю: {}", assigneeId);
        Page<Task> tasks = taskRepository.findByAssigneeId(assigneeId, pageable);
        log.info("Найдено {} задач для исполнителя {}", tasks.getTotalElements(), assigneeId);
        return tasks.map(this::convertToTaskDto);
    }

    /**
     * Находит задачу по ID.
     *
     * @param taskId ID задачи.
     * @return DTO задачи.
     */
    @Transactional(readOnly = true)
    public TaskDto findTaskById(Long taskId) {
        log.info("Поиск задачи по ID: {}", taskId);
        Task task = getTaskById(taskId);
        log.info("Задача найдена: {}", task.getTitle());
        return convertToTaskDto(task);
    }

    /**
     * Создает новую задачу.
     *
     * @param request данные для создания задачи.
     * @return DTO созданной задачи.
     */
    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Создание новой задачи пользователем: {}", currentUserId);
        log.debug("Параметры задачи: название={}, приоритет={}, количество исполнителей={}",
                request.getTitle(), request.getPriority(),
                request.getAssigneeIds() != null ? request.getAssigneeIds().size() : 0);

        Task.TaskBuilder taskBuilder = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(Task.TaskStatus.PENDING)
                .priority(request.getPriority())
                .authorId(currentUserId);

        // Добавляем исполнителей, если они указаны
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            request.getAssigneeIds().forEach(taskBuilder::addAssigneeId);
            log.info("Добавлены исполнители: {}", request.getAssigneeIds());
        }

        Task task = taskBuilder.build();

        Task savedTask = taskRepository.save(task);
        log.info("Задача успешно создана с ID: {}", savedTask.getId());

        return convertToTaskDto(savedTask);
    }

    /**
     * Обновляет задачу по ID.
     *
     * @param taskId ID задачи.
     * @param request данные для обновления задачи.
     * @return DTO обновленной задачи.
     */
    @Transactional
    public TaskDto updateTask(Long taskId, UpdateTaskRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Обновление задачи ID: {} пользователем: {}", taskId, currentUserId);

        Task task = getTaskById(taskId);
        log.debug("Текущий статус задачи: {}, приоритет: {}", task.getStatus(), task.getPriority());

        boolean isAdmin = SecurityUtils.hasRole("ROLE_ADMIN");
        boolean isAuthor = task.getAuthorId().equals(currentUserId);
        boolean isAssignee = task.getAssigneeIds().contains(currentUserId);
        log.debug("Права пользователя: admin={}, автор={}, исполнитель={}", isAdmin, isAuthor, isAssignee);

        // Если пользователь - исполнитель, он может обновлять только статус
        if (isAssignee && !isAdmin && !isAuthor) {
            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
                log.info("Статус задачи обновлен до {} исполнителем: {}", request.getStatus(), currentUserId);
            } else {
                log.warn("Попытка обновления задачи исполнителем: {}", currentUserId);
                throw new UnauthorizedException("Исполнитель может обновлять только статус задачи");
            }
        }

        // Админ или автор задачи может обновлять все поля
        else if (isAdmin || isAuthor) {
            if (request.getTitle() != null) {
                log.debug("Обновление названия задачи с '{}' на '{}'", task.getTitle(), request.getTitle());
                task.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                log.debug("Обновление описания задачи");
                task.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                log.debug("Обновление статуса задачи с '{}' на '{}'", task.getStatus(), request.getStatus());
                task.setStatus(request.getStatus());
            }
            if (request.getPriority() != null) {
                log.debug("Обновление приоритета задачи с '{}' на '{}'", task.getPriority(), request.getPriority());
                task.setPriority(request.getPriority());
            }
            if (request.getAssigneeIds() != null) {
                log.debug("Обновление списка исполнителей с {} на {}",
                        task.getAssigneeIds(), request.getAssigneeIds());
                task.setAssigneeIds(request.getAssigneeIds());
            }
            log.info("Задача обновлена пользователем {}: {}",
                    isAdmin ? "админ" : "автор", currentUserId);
        } else {
            log.warn("Попытка обновления задачи пользователем: {}", currentUserId);
            throw new UnauthorizedException("У вас нет прав на обновление этой задачи");
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Задача ID: {} успешно обновлена", updatedTask.getId());
        return convertToTaskDto(updatedTask);
    }

    /**
     * Удаляет задачу по ID.
     *
     * @param taskId ID задачи.
     */
    @Transactional
    public void deleteTask(Long taskId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Запрос на удаление задачи ID: {} пользователем: {}", taskId, currentUserId);

        Task task = getTaskById(taskId);

        boolean isAdmin = SecurityUtils.hasRole("ROLE_ADMIN");
        boolean isAuthor = task.getAuthorId().equals(currentUserId);

        if (isAdmin || isAuthor) {
            taskRepository.delete(task);
            log.info("Задача ID: {} удалена пользователем {}: {}",
                    taskId, isAdmin ? "админ" : "автор", currentUserId);
        } else {
            log.warn("Попытка удаления задачи пользователем: {}", currentUserId);
            throw new UnauthorizedException("У вас нет прав на удаление этой задачи");
        }
    }

    /**
     * Добавляет новый комментарий к задаче.
     *
     * @param taskId идентификатор задачи, к которой добавляется комментарий.
     * @param request объект с данными для создания комментария.
     * @return DTO комментария, содержащий информацию о добавленном комментарии.
     */
    @Transactional
    public CommentDto addComment(Long taskId, CreateCommentRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Добавление комментария к задаче ID: {} пользователем: {}", taskId, currentUserId);

        Task task = getTaskById(taskId);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .authorId(currentUserId)
                .task(task)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий успешно добавлен к задаче {} пользователем: {}", taskId, currentUserId);

        return convertToCommentDto(savedComment);
    }

    /**
     * Получает список комментариев для задачи.
     *
     * @param taskId идентификатор задачи, для которой нужно получить комментарии.
     * @return список DTO комментариев, привязанных к задаче.
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getTaskComments(Long taskId) {
        log.info("Получение комментариев для задачи ID: {}", taskId);

        getTaskById(taskId);

        List<Comment> comments = commentRepository.findByTaskId(taskId);
        log.info("Найдено {} комментариев для задачи ID: {}", comments.size(), taskId);

        return comments.stream()
                .map(this::convertToCommentDto)
                .collect(Collectors.toList());
    }

    /**
     * Получает задачу по идентификатору.
     *
     * @param taskId идентификатор задачи.
     * @return задачу с заданным идентификатором.
     * @throws ResourceNotFoundException если задача с заданным идентификатором не найдена.
     */
    private Task getTaskById(Long taskId) {
        log.debug("Поиск задачи с ID: {}", taskId);

        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Задача с ID: {} не найдена", taskId);
                    return new ResourceNotFoundException("Задача не найдена с ID: " + taskId);
                });
    }

    /**
     * Преобразует задачу в DTO.
     *
     * @param task задача, которую нужно преобразовать.
     * @return DTO задачи.
     */
    private TaskDto convertToTaskDto(Task task) {
        log.debug("Конвертация задачи в DTO: {}", task.getId());

        List<CommentDto> commentDtos = Optional.ofNullable(task.getComments())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::convertToCommentDto)
                .collect(Collectors.toList());

        String authorUsername = getUsernameById(task.getAuthorId());

        Map<String, String> assigneeUsernames = new HashMap<>();
        for (String assigneeId : task.getAssigneeIds()) {
            assigneeUsernames.put(assigneeId, getUsernameById(assigneeId));
        }

        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .authorId(task.getAuthorId())
                .authorUsername(authorUsername)
                .assigneeIds(task.getAssigneeIds())
                .assigneeUsernames(assigneeUsernames)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .comments(commentDtos)
                .build();
    }

    /**
     * Преобразует комментарий в DTO.
     *
     * @param comment комментарий, который нужно преобразовать.
     * @return DTO комментария.
     */
    private CommentDto convertToCommentDto(Comment comment) {
        log.debug("Конвертация комментария в DTO: {}", comment.getId());
        String authorUsername = getUsernameById(comment.getAuthorId());

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorUsername(authorUsername)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * Получает имя пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @return имя пользователя.
     */
    private String getUsernameById(String userId) {
        log.debug("Получение имени пользователя по ID: {}", userId);

        try {
            if (userCache.containsKey(userId)) {
                log.debug("Имя пользователя {} получено из кэша", userId);
                return userCache.get(userId).getUsername();
            }

            // Если нет в кэше, получаем из сервиса
            log.debug("Запрос информации о пользователе {} из сервиса пользователей", userId);
            UserInfoDto userInfo = userServiceClient.getUserInfo(userId);
            userCache.put(userId, userInfo);
            return userInfo.getUsername();
        } catch (Exception e) {
            log.error("Ошибка при получении информации о пользователе {}: {}", userId, e.getMessage());
            return "Неизвестный пользователь";
        }
    }

}
