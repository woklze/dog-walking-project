package com.project.dogwalking;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Хэширование пароля при регистрации
    public static String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Проверка пароля при входе
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
