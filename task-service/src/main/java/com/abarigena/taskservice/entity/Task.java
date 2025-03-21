package com.abarigena.taskservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сущность задачи, которая содержит информацию о задаче, её статусе, приоритетах и комментариях.
 * Каждая задача может быть связана с несколькими исполнителями и комментариями.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Column(nullable = false)
    private String authorId;

    /**
     * Список идентификаторов исполнителей задачи.
     */
    @ElementCollection
    @CollectionTable(name = "task_assignees", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "assignee_id")
    private Set<String> assigneeIds = new HashSet<>();

    /**
     * Время создания задачи.
     * Устанавливается автоматически при сохранении.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Время последнего обновления задачи.
     * Устанавливается автоматически при изменении.
     */
    private LocalDateTime updatedAt;

    /**
     * Список комментариев, связанных с задачей.
     * Каждый комментарий связан с этой задачей.
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED
    }

    public enum TaskPriority {
        HIGH, MEDIUM, LOW
    }

    /**
     * Метод, который вызывается перед сохранением сущности.
     * Устанавливает текущую дату и время в поля createdAt и updatedAt.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Метод, который вызывается перед обновлением сущности.
     * Обновляет поле updatedAt на текущее время.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private String title;
        private String description;
        private TaskStatus status;
        private TaskPriority priority;
        private String authorId;
        private Set<String> assigneeIds = new HashSet<>();

        public TaskBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskBuilder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder authorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public TaskBuilder assigneeIds(Set<String> assigneeIds) {
            this.assigneeIds = assigneeIds;
            return this;
        }

        public TaskBuilder addAssigneeId(String assigneeId) {
            this.assigneeIds.add(assigneeId);
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);
            task.setPriority(priority);
            task.setAuthorId(authorId);
            task.setAssigneeIds(assigneeIds);
            return task;
        }
    }
}
