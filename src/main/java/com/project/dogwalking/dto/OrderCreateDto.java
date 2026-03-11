package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateDto {

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Dog breed is required")
    private String dogBreed;

    private String dogNeeds; // особые потребности

    @NotNull(message = "Walk date and time is required")
    @Future(message = "Walk date must be in the future")
    private LocalDateTime walkDateTime;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    private Integer durationMinutes;

    @NotBlank(message = "Meeting point is required")
    private String meetingPoint;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment must be positive")
    private BigDecimal paymentAmount;
}
