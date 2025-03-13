package com.abarigena.taskservice.repository;
import com.abarigena.taskservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с задачами в базе данных.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>  {
    /**
     * Находит задачи по идентификатору автора с поддержкой пагинации.
     *
     * @param authorId Идентификатор автора.
     * @param pageable Параметры пагинации.
     * @return Страница задач, принадлежащих автору.
     */
    Page<Task> findByAuthorId(String authorId, Pageable pageable);

    /**
     * Находит задачи, назначенные конкретному исполнителю, с поддержкой пагинации.
     *
     * @param assigneeId Идентификатор исполнителя.
     * @param pageable Параметры пагинации.
     * @return Страница задач, назначенных на исполнителя.
     */
    @Query("SELECT t FROM Task t JOIN t.assigneeIds a WHERE a = :assigneeId")
    Page<Task> findByAssigneeId(String assigneeId, Pageable pageable);

    /**
     * Находит задачи по фильтрам с поддержкой пагинации.
     *
     * @param authorId Идентификатор автора задачи.
     * @param assigneeId Идентификатор исполнителя задачи.
     * @param status Статус задачи.
     * @param priority Приоритет задачи.
     * @param pageable Параметры пагинации.
     * @return Страница задач, удовлетворяющих фильтрам.
     */
    @Query("SELECT t FROM Task t WHERE " +
            "(:authorId IS NULL OR t.authorId = :authorId) AND " +
            "(:assigneeId IS NULL OR :assigneeId IN (SELECT a FROM t.assigneeIds a)) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByFilters(
            String authorId,
            String assigneeId,
            Task.TaskStatus status,
            Task.TaskPriority priority,
            Pageable pageable
    );
}
