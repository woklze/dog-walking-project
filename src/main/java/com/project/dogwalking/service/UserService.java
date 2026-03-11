package com.project.dogwalking.service;

import com.project.dogwalking.dto.UserRegistrationDto;
import com.project.dogwalking.dto.UserResponseDto;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.Role;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(UserRegistrationDto dto) {
        // проверяем, не занят ли email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessLogicException("Email already in use");
        }

        // создаём нового пользователя
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // преобразуем строку роли в enum
        try {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Invalid role. Use OWNER or WALKER");
        }

        user = userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // метод для обновления рейтинга
    @Transactional
    public void updateUserRating(Long userId) {
        // Здесь будет логика пересчёта среднего рейтинга
        // Но пока оставим заглушку, так как будем вызывать из RatingService
        // Реализуем позже, когда будет готов RatingService
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