package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Фильтр для поиска задач")
public class TaskFilterRequest {
    @Schema(description = "Идентификатор автора")
    private String authorId;
    @Schema(description = "Идентификатор исполнителя")
    private String assigneeId;
    @Schema(description = "Статус задачи", example = "COMPLETED")
    private Task.TaskStatus status;
    @Schema(description = "Приоритет задачи")
    private Task.TaskPriority priority;
    @Schema(description = "Номер страницы")
    private int page = 0;
    @Schema(description = "Количество элементов на странице", example = "10")
    private int size = 10;
}