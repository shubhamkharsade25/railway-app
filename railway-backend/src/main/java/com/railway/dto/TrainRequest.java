package com.railway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrainRequest(
        @NotBlank(message = "Train number is required")
        @Size(max = 10, message = "Train number must be at most 10 characters")
        String trainNumber,

        @NotBlank(message = "Train name is required")
        @Size(max = 100, message = "Train name must be at most 100 characters")
        String trainName,

        @NotBlank(message = "Train type is required")
        @Pattern(regexp = "EXPRESS|SUPERFAST|LOCAL|RAJDHANI|OTHER", message = "Train type must be EXPRESS, SUPERFAST, LOCAL, RAJDHANI, or OTHER")
        String trainType,

        @NotNull(message = "isActive flag is required")
        Boolean isActive
) {}
