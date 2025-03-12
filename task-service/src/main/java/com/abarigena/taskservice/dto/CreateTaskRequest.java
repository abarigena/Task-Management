package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String description;

    @NotNull(message = "Priority must be specified")
    private Task.TaskPriority priority;

    private Set<String> assigneeIds;
}
