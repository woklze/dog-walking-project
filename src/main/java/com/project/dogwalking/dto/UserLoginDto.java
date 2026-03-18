package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserLoginDto {
    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Неверный формат почты")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
