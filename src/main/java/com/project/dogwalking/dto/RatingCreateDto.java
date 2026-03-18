package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RatingCreateDto {

    @NotNull(message = "ID оценивающего пользователя не может быть пустым")
    private Long fromUserId;

    @NotNull(message = "ID оцениваемого пользователя не может быть пустым")
    private Long toUserId;

    @NotNull(message = "ID контракта не может быть пустым")
    private Long contractId;

    @NotNull(message = "Количество звезд не может быть пустым")
    @Min(1) @Max(5)
    private Integer stars;

    @Size(max = 500, message = "Комментарий слишком длинный")
    private String comment;
}
