package com.project.dogwalking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String username;
    private String role;        // OWNER или WALKER
    private Double rating;
    private LocalDateTime createdAt;
}