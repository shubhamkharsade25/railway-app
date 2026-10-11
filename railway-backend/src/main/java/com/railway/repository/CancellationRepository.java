package com.railway.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CancellationRepository {

    private final JdbcTemplate jdbc;

    public CancellationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lock booking row for update.
     */
    public Optional<Map<String, Object>> findBookingForUpdate(String pnr) {
        String sql = """
                SELECT b.Booking_ID, b.PNR, b.Status, b.Train_ID, b.Source_Station_ID,
                       b.Journey_Date, b.Total_Fare, ts.Departure_Time
                FROM BOOKING b
                JOIN TRAIN_STATION ts ON ts.Train_ID = b.Train_ID AND ts.Station_ID = b.Source_Station_ID
                WHERE b.PNR = ?
                FOR UPDATE
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, pnr);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void updateBookingStatus(int bookingId, String status) {
        jdbc.update("UPDATE BOOKING SET Status = ? WHERE Booking_ID = ?", status, bookingId);
    }

    public void cancelReservations(int bookingId) {
        jdbc.update("UPDATE SEAT_RESERVATION SET Status = 'CANCELLED', Active_Flag = NULL WHERE Booking_ID = ?", bookingId);
    }

    public void insertCancellation(int bookingId, String reason, BigDecimal refundAmount) {
        jdbc.update("""
                INSERT INTO CANCELLATION (Booking_ID, Cancelled_At, Reason, Refund_Amount)
                VALUES (?, NOW(), ?, ?)
                """, bookingId, reason, refundAmount);
    }

    public void updatePaymentStatus(int bookingId, String status) {
        jdbc.update("UPDATE PAYMENT SET Payment_Status = ? WHERE Booking_ID = ?", status, bookingId);
    }
}
