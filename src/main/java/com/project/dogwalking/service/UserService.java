package com.project.dogwalking.service;

import com.project.dogwalking.PasswordHashUtil;
import com.project.dogwalking.dto.UserRegistrationDto;
import com.project.dogwalking.dto.UserResponseDto;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.Role;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto register(UserRegistrationDto dto) {
        // Хэшируем пароль перед сохранением
        String hashedPassword = PasswordHashUtil.hashPassword(dto.getPassword());

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(hashedPassword); // Храним ТОЛЬКО хэш

        try {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Несуществующая роль. ИСпользуйте OWNER или WALKER");
        }

        userRepository.save(user);
        return null;
    }

    public boolean authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return false;
        }
        return userRepository.findByEmail(email)
                .map(user -> PasswordHashUtil.verifyPassword(rawPassword, user.getPasswordHash()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден пользователь с ID: " + id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден пользователь с ID: " + id));
    }

    // маппинг из сущности в dto
    private UserResponseDto mapToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setRating(user.getRating());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
