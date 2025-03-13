package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Запрос на создание задачи")
public class CreateTaskRequest {
    @NotBlank(message = "Title cannot be empty")
    @Schema(description = "Название задачи")
    private String title;

    @Schema(description = "Описание задачи")
    private String description;

    @Schema(description = "Приоритет задачи")
    @NotNull(message = "Priority must be specified")
    private Task.TaskPriority priority;

    @Schema(description = "Список идентификаторов исполнителей", example = "[\"123\", \"456\"]")
    private Set<String> assigneeIds;
}
