package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
import lombok.Data;

@Data
public class TaskFilterRequest {
    private String authorId;
    private String assigneeId;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private String searchQuery;
    private int page = 0;
    private int size = 10;
}