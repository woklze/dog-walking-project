package com.project.dogwalking.dto;

import com.project.dogwalking.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String username;
    private String role;
    private Double rating;
    private LocalDateTime createdAt;

}
