package com.project.dogwalking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class RatingCreateDto {

    @NotNull(message = "From user ID is required")
    private Long fromUserId;

    @NotNull(message = "To user ID is required")
    private Long toUserId;

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotNull(message = "Stars are required")
    @Min(1) @Max(5)
    private Integer stars;

    @Size(max = 500, message = "Comment too long")
    private String comment;
}