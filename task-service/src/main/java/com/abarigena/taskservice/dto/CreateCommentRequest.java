package com.abarigena.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание комментария")
public class CreateCommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    @Schema(description = "Содержимое комментария", example = "Это новый комментарий")
    private String content;
}