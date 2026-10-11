package com.railway.repository;

import com.railway.dto.BookingResponse;
import com.railway.dto.TicketDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BookingRepository {

    private final JdbcTemplate jdbc;

    public BookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lock available seats with FOR UPDATE SKIP LOCKED to prevent double-booking.
     */
    public List<Integer> lockAvailableSeats(int trainId, String classType, LocalDate journeyDate, int limit) {
        String sql = """
                SELECT s.Seat_ID FROM SEAT s
                JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE c.Train_ID = ? AND c.Class_Type = ?
                  AND NOT EXISTS (
                    SELECT 1 FROM SEAT_RESERVATION sr
                    WHERE sr.Seat_ID = s.Seat_ID AND sr.Journey_Date = ? AND sr.Status = 'ACTIVE')
                ORDER BY c.Coach_Number, s.Seat_Number
                LIMIT ? FOR UPDATE SKIP LOCKED
                """;
        return jdbc.query(sql, (rs, rn) -> rs.getInt("Seat_ID"), trainId, classType, journeyDate, limit);
    }

    /**
     * Insert a new booking row and return the generated Booking_ID.
     */
    public int insertBooking(String pnr, int trainId, int sourceStationId, int destinationStationId,
                             String classType, LocalDate journeyDate, String status,
                             BigDecimal totalFare, String contactEmail, String contactPhone) {
        String sql = """
                INSERT INTO BOOKING (PNR, Train_ID, Source_Station_ID, Destination_Station_ID,
                    Class_Type, Journey_Date, Status, Total_Fare, Contact_Email, Contact_Phone)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pnr);
            ps.setInt(2, trainId);
            ps.setInt(3, sourceStationId);
            ps.setInt(4, destinationStationId);
            ps.setString(5, classType);
            ps.setDate(6, Date.valueOf(journeyDate));
            ps.setString(7, status);
            ps.setBigDecimal(8, totalFare);
            ps.setString(9, contactEmail);
            ps.setString(10, contactPhone);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Insert a passenger and return the generated Passenger_ID.
     */
    public int insertPassenger(String name, int age, String gender) {
        String sql = "INSERT INTO PASSENGER (Passenger_Name, Age, Gender) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Link passenger to booking in the join table.
     */
    public void insertBookingPassenger(int bookingId, int passengerId) {
        jdbc.update("INSERT INTO BOOKING_PASSENGER (Booking_ID, Passenger_ID) VALUES (?, ?)",
                bookingId, passengerId);
    }

    /**
     * Reserve a seat (ACTIVE, Active_Flag=1).
     */
    public int insertSeatReservation(int bookingId, int passengerId, int seatId, LocalDate journeyDate) {
        String sql = """
                INSERT INTO SEAT_RESERVATION (Booking_ID, Passenger_ID, Seat_ID, Journey_Date, Status, Active_Flag)
                VALUES (?, ?, ?, ?, 'ACTIVE', 1)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setInt(2, passengerId);
            ps.setInt(3, seatId);
            ps.setDate(4, Date.valueOf(journeyDate));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Insert a payment record and return generated Payment_ID.
     */
    public int insertPayment(int bookingId, BigDecimal amount, String paymentMethod, String paymentStatus) {
        String sql = """
                INSERT INTO PAYMENT (Booking_ID, Amount, Payment_Method, Payment_Status, Paid_At)
                VALUES (?, ?, ?, ?, NOW())
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, paymentMethod);
            ps.setString(4, paymentStatus);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Update booking status (e.g. to FAILED).
     */
    public void updateBookingStatus(int bookingId, String status) {
        jdbc.update("UPDATE BOOKING SET Status = ? WHERE Booking_ID = ?", status, bookingId);
    }

    /**
     * Cancel all seat reservations for a booking (set CANCELLED, Active_Flag=NULL).
     */
    public void cancelReservations(int bookingId) {
        jdbc.update("""
                UPDATE SEAT_RESERVATION SET Status = 'CANCELLED', Active_Flag = NULL
                WHERE Booking_ID = ?
                """, bookingId);
    }

    /**
     * Find a booking by PNR — returns raw row map for the booking header.
     */
    public Optional<Map<String, Object>> findByPnr(String pnr) {
        String sql = """
                SELECT b.Booking_ID, b.PNR, b.Status, b.Train_ID, b.Source_Station_ID,
                       b.Destination_Station_ID, b.Class_Type, b.Journey_Date,
                       b.Total_Fare, b.Contact_Email, b.Contact_Phone, b.Booked_At,
                       t.Train_Number, t.Train_Name, t.Train_Type,
                       ss.Station_Name AS Source_Station_Name, ss.Station_Code AS Source_Station_Code,
                       ds.Station_Name AS Dest_Station_Name, ds.Station_Code AS Dest_Station_Code
                FROM BOOKING b
                JOIN TRAIN t ON t.Train_ID = b.Train_ID
                JOIN STATION ss ON ss.Station_ID = b.Source_Station_ID
                JOIN STATION ds ON ds.Station_ID = b.Destination_Station_ID
                WHERE b.PNR = ?
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, pnr);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> findAllBookings(String status) {
        String baseSql = """
                SELECT b.Booking_ID AS bookingId, b.PNR AS pnr, b.Status AS status,
                       t.Train_Number AS trainNumber, t.Train_Name AS trainName,
                       ss.Station_Name AS sourceStationName, ds.Station_Name AS destStationName,
                       b.Class_Type AS classType, b.Journey_Date AS journeyDate,
                       b.Total_Fare AS totalFare, b.Booked_At AS bookedAt,
                       b.Contact_Email AS contactEmail, b.Contact_Phone AS contactPhone,
                       p.Payment_Status AS paymentStatus
                FROM BOOKING b
                JOIN TRAIN t ON t.Train_ID = b.Train_ID
                JOIN STATION ss ON ss.Station_ID = b.Source_Station_ID
                JOIN STATION ds ON ds.Station_ID = b.Destination_Station_ID
                LEFT JOIN PAYMENT p ON p.Booking_ID = b.Booking_ID
                """;
        if (status != null && !status.isBlank()) {
            return jdbc.queryForList(baseSql + " WHERE b.Status = ? ORDER BY b.Booked_At DESC", status.trim().toUpperCase());
        }
        return jdbc.queryForList(baseSql + " ORDER BY b.Booked_At DESC");
    }

    public void updateBookingDetails(int bookingId, String status, String email, String phone) {
        StringBuilder sql = new StringBuilder("UPDATE BOOKING SET ");
        List<Object> params = new ArrayList<>();
        boolean first = true;

        if (status != null && !status.isBlank()) {
            sql.append("Status = ?");
            params.add(status.trim().toUpperCase());
            first = false;
        }
        if (email != null && !email.isBlank()) {
            if (!first) sql.append(", ");
            sql.append("Contact_Email = ?");
            params.add(email.trim());
            first = false;
        }
        if (phone != null && !phone.isBlank()) {
            if (!first) sql.append(", ");
            sql.append("Contact_Phone = ?");
            params.add(phone.trim());
            first = false;
        }

        if (params.isEmpty()) return;

        sql.append(" WHERE Booking_ID = ?");
        params.add(bookingId);

        jdbc.update(sql.toString(), params.toArray());
    }

    /**
     * Delete a booking completely from the database, freeing all reservations and cleaning related tables.
     */
    public void deleteBookingCascade(int bookingId) {
        // 1. Delete cancellation record if exists
        jdbc.update("DELETE FROM CANCELLATION WHERE Booking_ID = ?", bookingId);
        // 2. Delete payments
        jdbc.update("DELETE FROM PAYMENT WHERE Booking_ID = ?", bookingId);
        // 3. Delete seat reservations (frees seats instantly)
        jdbc.update("DELETE FROM SEAT_RESERVATION WHERE Booking_ID = ?", bookingId);
        // 4. Delete booking-passenger junction
        jdbc.update("DELETE FROM BOOKING_PASSENGER WHERE Booking_ID = ?", bookingId);
        // 5. Delete booking row
        jdbc.update("DELETE FROM BOOKING WHERE Booking_ID = ?", bookingId);
    }

    /**
     * Get passengers with seat info for a booking.
     */
    public List<Map<String, Object>> findPassengersWithSeats(int bookingId) {
        String sql = """
                SELECT p.Passenger_ID, p.Passenger_Name, p.Age, p.Gender,
                       c.Coach_Number, s.Seat_Number, s.Seat_Type, s.Seat_ID,
                       sr.Status AS ReservationStatus
                FROM BOOKING_PASSENGER bp
                JOIN PASSENGER p ON p.Passenger_ID = bp.Passenger_ID
                LEFT JOIN SEAT_RESERVATION sr ON sr.Booking_ID = bp.Booking_ID AND sr.Passenger_ID = bp.Passenger_ID
                LEFT JOIN SEAT s ON s.Seat_ID = sr.Seat_ID
                LEFT JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE bp.Booking_ID = ?
                """;
        return jdbc.queryForList(sql, bookingId);
    }

    /**
     * Get payment info for a booking.
     */
    public List<Map<String, Object>> findPayments(int bookingId) {
        String sql = """
                SELECT Payment_ID, Amount, Payment_Method, Payment_Status, Paid_At
                FROM PAYMENT WHERE Booking_ID = ?
                """;
        return jdbc.queryForList(sql, bookingId);
    }

    /**
     * Get departure time from source and arrival time at destination (for ticket).
     */
    public Map<String, Object> findRouteTimes(int trainId, int sourceStationId, int destStationId) {
        String sql = """
                SELECT src.Departure_Time AS departure_time, dst.Arrival_Time AS arrival_time
                FROM TRAIN_STATION src, TRAIN_STATION dst
                WHERE src.Train_ID = ? AND src.Station_ID = ?
                  AND dst.Train_ID = ? AND dst.Station_ID = ?
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, trainId, sourceStationId, trainId, destStationId);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }
}
