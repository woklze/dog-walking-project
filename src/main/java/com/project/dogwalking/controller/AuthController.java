package com.project.dogwalking.controller;

import com.project.dogwalking.dto.UserLoginDto;
import com.project.dogwalking.dto.UserRegistrationDto;
import com.project.dogwalking.dto.UserResponseDto;
import com.project.dogwalking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserResponseDto createdUser = userService.register(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        // Пока просто заглушка: ищем пользователя по email, проверяем пароль
        // Для демо можно возвращать пользователя, но в реальности здесь должен быть JWT токен
        // Так как авторизация не обязательна для минимальной версии, можно оставить заглушку,
        // либо вообще убрать этот метод.
        // Для простоты вернём пользователя без проверки пароля (небезопасно!).
        // Лучше пока закомментировать или удалить.
        throw new UnsupportedOperationException("Login not implemented yet");
    }
}