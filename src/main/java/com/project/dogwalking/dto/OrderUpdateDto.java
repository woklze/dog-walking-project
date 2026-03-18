package com.project.dogwalking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderUpdateDto {

    @NotBlank(message = "Порода собаки обязательна")
    private String dogBreed;

    private String dogNeeds;

    @NotNull(message = "Дата и время выгула обязательны")
    @Future(message = "Дата выгула должна быть в будущем")
    private LocalDateTime walkDateTime;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 15, message = "Длительность должна быть не менее 15 минут")
    private Integer durationMinutes;

    @NotBlank(message = "Место встречи обязательно")
    private String meetingPoint;

    @NotNull(message = "Сумма оплаты обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Сумма должна быть положительной")
    private BigDecimal paymentAmount;
}
