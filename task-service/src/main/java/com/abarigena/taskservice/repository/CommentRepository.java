package com.abarigena.taskservice.repository;

import com.abarigena.taskservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с комментариями в базе данных.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Находит все комментарии для задачи по её ID.
     *
     * @param taskId Идентификатор задачи.
     * @return Список комментариев для указанной задачи.
     */
    List<Comment> findByTaskId(Long taskId);
}