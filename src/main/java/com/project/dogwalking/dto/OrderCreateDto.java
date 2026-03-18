package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateDto {

    @NotNull(message = "ID владельца не может быть пустым")
    private Long ownerId;

    @NotBlank(message = "Порода собаки не может быть пустой")
    private String dogBreed;

    private String dogNeeds;

    @NotNull(message = "Время прогулки не может быть пустым")
    @Future(message = "Время прогулки должно быть в будущем")
    private LocalDateTime walkDateTime;

    @NotNull(message = "Продолжительность прогулки не может быть пустой")
    @Min(value = 15, message = "Продолжительность прогулки должна быть как минимум 15 минут")
    private Integer durationMinutes;

    @NotBlank(message = "Место встречи не может быть пустым")
    private String meetingPoint;

    @NotNull(message = "Сумма оплаты не может быть пустой")
    @DecimalMin(value = "0.0", inclusive = false, message = "Сумма оплаты должна быть положительной")
    private BigDecimal paymentAmount;
}
