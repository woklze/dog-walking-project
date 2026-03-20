package com.project.dogwalking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContractResponseDto {
    private Long id;
    private Long orderId;
    private Long walkerId;
    private String walkerName;
    private String status;
    private Boolean prepaid;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
