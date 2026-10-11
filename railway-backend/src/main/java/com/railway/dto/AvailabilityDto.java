package com.railway.dto;

import java.math.BigDecimal;
import java.util.List;

public record AvailabilityDto(
        Integer trainId,
        String classType,
        String journeyDate,
        Integer totalSeats,
        Integer availableSeats,
        List<SeatInfo> seats
) {
    public record SeatInfo(
            Integer seatId,
            String coachNumber,
            Integer seatNumber,
            String seatType
    ) {}
}
