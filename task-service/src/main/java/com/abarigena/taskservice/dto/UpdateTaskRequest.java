package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private String assigneeId;
}