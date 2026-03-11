package com.project.dogwalking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingResponseDto {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Long contractId;
    private Integer stars;
    private String comment;
    private LocalDateTime createdAt;
}