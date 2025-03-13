package com.abarigena.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о пользователе")
public class UserInfoDto {
    @Schema(description = "Идентификатор пользователя")
    private String id;
    @Schema(description = "Имя пользователя")
    private String username;
    @Schema(description = "Email пользователя")
    private String email;
}