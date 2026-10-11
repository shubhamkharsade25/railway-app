package com.railway.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record RouteStopRequest(
        @NotNull(message = "Station ID is required")
        Integer stationId,

        @NotNull(message = "Stop number is required")
        @Min(value = 1, message = "Stop number must be at least 1")
        Integer stopNo,

        LocalTime arrivalTime,
        LocalTime departureTime,

        @NotNull(message = "Distance in KM is required")
        @Min(value = 0, message = "Distance cannot be negative")
        Integer distanceKm
) {}
