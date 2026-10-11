package com.railway.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CoachRequest(
        @NotBlank(message = "Coach number is required")
        @Size(max = 5, message = "Coach number must be at most 5 characters")
        String coachNumber,

        @NotBlank(message = "Class type is required")
        @Pattern(regexp = "SL|3A|2A|1A|CC", message = "Class type must be SL, 3A, 2A, 1A, or CC")
        String classType,

        @NotNull(message = "Seat count is required")
        @Min(value = 1, message = "Coach must have at least 1 seat")
        @Max(value = 120, message = "Coach cannot have more than 120 seats")
        Integer seatCount
) {}
