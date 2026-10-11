package com.railway.service;

import com.railway.dto.CancellationRequest;
import com.railway.dto.CancellationResponse;
import com.railway.exception.ConflictException;
import com.railway.exception.ResourceNotFoundException;
import com.railway.repository.CancellationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
public class CancellationService {

    private final CancellationRepository cancellationRepository;
    private final Clock clock;

    public CancellationService(CancellationRepository cancellationRepository, Clock clock) {
        this.cancellationRepository = cancellationRepository;
        this.clock = clock;
    }

    /**
     * Cancellation transaction:
     * 1. Lock booking FOR UPDATE
     * 2. Verify status CONFIRMED and departure in future
     * 3. Compute refund according to tiers
     * 4. Update booking, seat reservation, cancellation and payment records
     */
    @Transactional
    public CancellationResponse cancelBooking(String pnr, CancellationRequest req) {
        Map<String, Object> booking = cancellationRepository.findBookingForUpdate(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for PNR: " + pnr));

        String status = (String) booking.get("Status");
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            throw new ConflictException("Cannot cancel booking with status: " + status);
        }

        int bookingId = ((Number) booking.get("Booking_ID")).intValue();
        BigDecimal totalFare = new BigDecimal(booking.get("Total_Fare").toString());

        LocalDate journeyDate = toLocalDate(booking.get("Journey_Date"));
        LocalTime departureTime = toLocalTime(booking.get("Departure_Time"));
        if (departureTime == null) {
            departureTime = LocalTime.MIDNIGHT;
        }

        LocalDateTime departureDateTime = LocalDateTime.of(journeyDate, departureTime);
        LocalDateTime now = LocalDateTime.now(clock);

        if (now.isAfter(departureDateTime)) {
            throw new ConflictException("Cannot cancel booking for a train that has already departed");
        }

        Duration timeUntilDeparture = Duration.between(now, departureDateTime);
        long hoursUntilDeparture = timeUntilDeparture.toHours();

        BigDecimal refundPercentage;
        if (hoursUntilDeparture >= 48) {
            refundPercentage = new BigDecimal("0.90");
        } else if (hoursUntilDeparture >= 12) {
            refundPercentage = new BigDecimal("0.50");
        } else {
            refundPercentage = BigDecimal.ZERO;
        }

        BigDecimal refundAmount = totalFare.multiply(refundPercentage).setScale(2, RoundingMode.HALF_UP);

        cancellationRepository.updateBookingStatus(bookingId, "CANCELLED");
        cancellationRepository.cancelReservations(bookingId);
        cancellationRepository.insertCancellation(bookingId, req.reason(), refundAmount);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            cancellationRepository.updatePaymentStatus(bookingId, "REFUNDED");
        }

        return new CancellationResponse(pnr, "CANCELLED", refundAmount, now);
    }

    private static LocalDate toLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate ld) return ld;
        if (obj instanceof java.sql.Date d) return d.toLocalDate();
        return null;
    }

    private static LocalTime toLocalTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalTime lt) return lt;
        if (obj instanceof Time t) return t.toLocalTime();
        return null;
    }
}
