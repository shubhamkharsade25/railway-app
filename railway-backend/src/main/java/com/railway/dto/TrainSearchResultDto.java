package com.railway.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record TrainSearchResultDto(
        Integer trainId,
        String trainNumber,
        String trainName,
        String trainType,
        LocalTime departureTime,
        LocalTime arrivalTime,
        Integer distanceKm,
        String durationFormatted,
        List<ClassAvailabilityDto> classAvailability
) {
    public record ClassAvailabilityDto(
            String classType,
            BigDecimal fare,
            Integer availableSeats,
            Integer totalSeats
    ) {}
}
