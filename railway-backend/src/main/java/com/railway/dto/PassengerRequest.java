package com.railway.dto;

import jakarta.validation.constraints.*;

public record PassengerRequest(
        @NotBlank(message = "Passenger name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be at least 0")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "M|F|O", message = "Gender must be M, F, or O")
        String gender,

        Integer preferredSeatId
) {}
