package com.abarigena.taskservice.repository;
import com.abarigena.taskservice.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>  {
    Page<Task> findByAuthorId(String authorId, Pageable pageable);
    Page<Task> findByAssigneeId(String assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
            "(:authorId IS NULL OR t.authorId = :authorId) AND " +
            "(:assigneeId IS NULL OR t.assigneeId = :assigneeId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority)")
    Page<Task> findByFilters(
            String authorId,
            String assigneeId,
            Task.TaskStatus status,
            Task.TaskPriority priority,
            String searchQuery, // Не используется в запросе
            Pageable pageable
    );
}
