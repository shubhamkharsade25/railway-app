package com.railway.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Integer bookingId,
        String pnr,
        String status,
        Integer trainId,
        String trainNumber,
        String trainName,
        Integer sourceStationId,
        String sourceStationName,
        Integer destinationStationId,
        String destinationStationName,
        LocalDate journeyDate,
        String classType,
        BigDecimal totalFare,
        String contactEmail,
        String contactPhone,
        LocalDateTime bookedAt,
        List<PassengerTicketInfo> passengers,
        PaymentInfo payment
) {
    public record PassengerTicketInfo(
            Integer passengerId,
            String name,
            Integer age,
            String gender,
            String coachNumber,
            Integer seatNumber,
            String seatType,
            Integer seatId
    ) {}

    public record PaymentInfo(
            Integer paymentId,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus,
            LocalDateTime paidAt
    ) {}
}
