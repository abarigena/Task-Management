package com.abarigena.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Комментарий к задаче")
public class CommentDto {
    private Long id;

    @Schema(description = "Содержимое комментария")
    private String content;
    @Schema(description = "Идентификатор автора комментария")
    private String authorId;
    @Schema(description = "Имя пользователя автора комментария")
    private String authorUsername;
    @Schema(description = "Дата и время создания комментария")
    private LocalDateTime createdAt;
}