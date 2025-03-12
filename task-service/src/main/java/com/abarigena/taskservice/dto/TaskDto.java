package com.abarigena.taskservice.dto;

import com.abarigena.taskservice.entity.Task;
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
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private String authorId;
    private String authorUsername;

    private Set<String> assigneeIds;
    private Map<String, String> assigneeUsernames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDto> comments;
}