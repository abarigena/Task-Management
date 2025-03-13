package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Запрос на обновление задачи")
public class UpdateTaskRequest {
    @Schema(description = "Название задачи")
    private String title;
    @Schema(description = "Описание задачи")
    private String description;
    @Schema(description = "Статус задачи")
    private Task.TaskStatus status;
    @Schema(description = "Приоритет задачи")
    private Task.TaskPriority priority;
    @Schema(description = "Список идентификаторов исполнителей", example = "[\"456\", \"789\"]")
    private Set<String> assigneeIds;
}