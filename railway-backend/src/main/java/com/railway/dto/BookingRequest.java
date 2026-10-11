package com.railway.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record BookingRequest(
        @NotNull(message = "Train ID is required")
        Integer trainId,

        @NotNull(message = "Source station ID is required")
        Integer sourceStationId,

        @NotNull(message = "Destination station ID is required")
        Integer destinationStationId,

        @NotNull(message = "Journey date is required")
        @FutureOrPresent(message = "Journey date must be today or in the future")
        LocalDate journeyDate,

        @NotBlank(message = "Class type is required")
        @Pattern(regexp = "SL|3A|2A|1A|CC", message = "Class type must be SL, 3A, 2A, 1A, or CC")
        String classType,

        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid email format")
        String contactEmail,

        @NotBlank(message = "Contact phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
        String contactPhone,

        @NotBlank(message = "Payment method is required")
        @Pattern(regexp = "UPI|CARD|NETBANKING|CASH", message = "Payment method must be UPI, CARD, NETBANKING, or CASH")
        String paymentMethod,

        @NotBlank(message = "Simulated payment status is required")
        @Pattern(regexp = "SUCCESS|FAILED|PENDING", message = "Payment status must be SUCCESS, FAILED, or PENDING")
        String simulatedPaymentStatus,

        @NotNull(message = "Passengers list is required")
        @Size(min = 1, max = 6, message = "Number of passengers must be between 1 and 6")
        @Valid
        List<PassengerRequest> passengers
) {}
