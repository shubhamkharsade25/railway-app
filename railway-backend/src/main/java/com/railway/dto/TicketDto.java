package com.railway.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record TicketDto(
        String pnr,
        String bookingStatus,
        TrainInfo train,
        StationInfo source,
        StationInfo destination,
        LocalDate journeyDate,
        String classType,
        BigDecimal totalFare,
        String contactEmail,
        String contactPhone,
        LocalDateTime bookedAt,
        List<PassengerTicketRow> passengers,
        PaymentInfo payment
) {
    public record TrainInfo(
            Integer trainId,
            String trainNumber,
            String trainName,
            String trainType
    ) {}

    public record StationInfo(
            Integer stationId,
            String stationName,
            String stationCode,
            LocalTime departureOrArrivalTime
    ) {}

    public record PassengerTicketRow(
            String name,
            Integer age,
            String gender,
            String coachNumber,
            Integer seatNumber,
            String seatType
    ) {}

    public record PaymentInfo(
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    ) {}
}
