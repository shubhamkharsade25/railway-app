package com.railway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBookingRequest(
        @Pattern(regexp = "CONFIRMED|CANCELLED|FAILED", message = "Status must be CONFIRMED, CANCELLED, or FAILED")
        String status,

        @Email(message = "Invalid email format")
        @Size(max = 120, message = "Email cannot exceed 120 characters")
        String contactEmail,

        @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
        String contactPhone
) {}
