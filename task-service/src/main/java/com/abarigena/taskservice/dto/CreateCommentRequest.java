package com.abarigena.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;
}