package com.project.dogwalking;

import com.project.dogwalking.dto.UserRegistrationDto;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.Role;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.UserRepository;
import com.project.dogwalking.service.UserService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ========== Нормальные сценарии ==========
    @Test
    void register_ValidOwner_Success() {
        UserRegistrationDto dto = createDto("owner@test.com", "owner", "pass", "OWNER");
        userService.register(dto);
        User saved = userRepository.findByEmail("owner@test.com").orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("owner");
        assertThat(saved.getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsTrue() {
        userService.register(createDto("auth@test.com", "auth", "secret", "WALKER"));
        assertTrue(userService.authenticate("auth@test.com", "secret"));
    }

    // ========== Аномальные сценарии (реальные ограничения) ==========
    @Test
    void register_NullEmail_ThrowsDataIntegrityViolation() {
        UserRegistrationDto dto = createDto(null, "user", "pass", "OWNER");
        assertThrows(DataIntegrityViolationException.class, () -> userService.register(dto));
    }

    @Test
    void register_NullUsername_ThrowsDataIntegrityViolation() {
        UserRegistrationDto dto = createDto("email@test.com", null, "pass", "OWNER");
        assertThrows(DataIntegrityViolationException.class, () -> userService.register(dto));
    }


    @Test
    void register_EmptyUsername_ThrowsDataIntegrityViolation() {
        UserRegistrationDto dto = createDto("empty@test.com", "", "pass", "OWNER");
        assertThrows(DataIntegrityViolationException.class, () -> userService.register(dto));
    }


    @Test
    void register_DuplicateEmail_ThrowsDataIntegrityViolation() {
        userService.register(createDto("dup@test.com", "user1", "pass", "OWNER"));
        UserRegistrationDto duplicate = createDto("dup@test.com", "user2", "pass", "WALKER");
        assertThrows(DataIntegrityViolationException.class, () -> userService.register(duplicate));
    }

    @Test
    void register_InvalidRole_ThrowsBusinessLogicException() {
        UserRegistrationDto dto = createDto("bad@test.com", "bad", "pass", "ADMIN");
        assertThrows(BusinessLogicException.class, () -> userService.register(dto));
    }

    @Test
    void authenticate_NonExistentEmail_ReturnsFalse() {
        assertFalse(userService.authenticate("nonexistent@test.com", "any"));
    }

    @Test
    void authenticate_WrongPassword_ReturnsFalse() {
        userService.register(createDto("wrongpass@test.com", "wpass", "correct", "OWNER"));
        assertFalse(userService.authenticate("wrongpass@test.com", "incorrect"));
    }

    @Test
    void authenticate_NullEmail_ReturnsFalse() {
        assertFalse(userService.authenticate(null, "pass"));
    }

    @Test
    void getUserById_NonExistent_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getUserById_NegativeId_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(-1L));
    }

    @Test
    void findUserEntityById_NonExistent_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserEntityById(999L));
    }

    // Вспомогательный метод
    private UserRegistrationDto createDto(String email, String username, String password, String role) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail(email);
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setRole(role);
        return dto;
    }
}
