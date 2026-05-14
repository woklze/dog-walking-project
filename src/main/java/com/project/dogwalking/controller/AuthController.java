package com.project.dogwalking.controller;

import com.project.dogwalking.dto.UserLoginDto;
import com.project.dogwalking.dto.UserRegistrationDto;
import com.project.dogwalking.dto.UserResponseDto;
import com.project.dogwalking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto regDto) {
        userService.register(regDto);
        return ResponseEntity.ok("Пользователь зарегистрирован");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto, HttpSession session) {
        if (userService.authenticate(loginDto.getEmail(), loginDto.getPassword())) {
            session.setAttribute("userEmail", loginDto.getEmail());
            session.setAttribute("isAuthenticated", true);
            session.setMaxInactiveInterval(3600); // Таймаут 1 час (в секундах)
            return ResponseEntity.ok("Успешный вход");
        } else {
            return ResponseEntity.status(401).body("Неверные учетные данные");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();
        return ResponseEntity.ok("Успешный выход из системы");
    }
}
