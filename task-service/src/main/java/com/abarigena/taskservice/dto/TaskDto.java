package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о задаче")
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private String authorId;
    private String authorUsername;

    @Schema(description = "Идентификаторы исполнителей", example = "[\"456\", \"789\"]")
    private Set<String> assigneeIds;
    @Schema(description = "Сопоставление идентификаторов исполнителей с их именами",
            example = "{\"456\": \"user1\", \"789\": \"user2\"}")
    private Map<String, String> assigneeUsernames;
    @Schema(description = "Дата и время создания задачи")
    private LocalDateTime createdAt;
    @Schema(description = "Дата и время последнего обновления задачи")
    private LocalDateTime updatedAt;
    @Schema(description = "Список комментариев к задаче")
    private List<CommentDto> comments;
}