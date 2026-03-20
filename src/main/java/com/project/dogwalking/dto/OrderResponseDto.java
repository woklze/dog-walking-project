package com.project.dogwalking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponseDto {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String dogBreed;
    private String dogNeeds;
    private LocalDateTime walkDateTime;
    private Integer durationMinutes;
    private String meetingPoint;
    private BigDecimal paymentAmount;
    private String status;
    private LocalDateTime createdAt;
}
