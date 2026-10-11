package com.railway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StationRequest(
        @NotBlank(message = "Station name is required")
        @Size(max = 100, message = "Station name must be at most 100 characters")
        String stationName,

        @NotBlank(message = "Station code is required")
        @Pattern(regexp = "^[A-Z]{2,10}$", message = "Station code must be 2-10 uppercase letters")
        String stationCode,

        @NotBlank(message = "City is required")
        @Size(max = 60, message = "City must be at most 60 characters")
        String city
) {}
