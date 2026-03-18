package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Неверный формат почты")
    private String email;

    @NotBlank(message = "Ник не может быть пустым")
    @Size(min = 3, max = 50, message = "Ник должен быть не менее 3 символов и не более 50")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть более 5 символов")
    private String password;

    @NotBlank(message = "Роль не может быть пустой")
    private String role; // "OWNER" или "WALKER"
}
