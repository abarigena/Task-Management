package com.abarigena.taskservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Информация о пользователе")
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    @Schema(description = "Идентификатор пользователя")
    private String id;
    @Schema(description = "Имя пользователя")
    private String username;
    @Schema(description = "Email пользователя")
    private String email;
}