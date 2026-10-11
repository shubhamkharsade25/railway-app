package com.railway.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> getSystemSummary() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM STATION) AS totalStations,
                    (SELECT COUNT(*) FROM TRAIN WHERE Is_Active = 1) AS totalActiveTrains,
                    (SELECT COUNT(*) FROM BOOKING WHERE Status = 'CONFIRMED') AS confirmedBookings,
                    (SELECT COUNT(*) FROM BOOKING WHERE Status = 'CANCELLED') AS cancelledBookings,
                    (SELECT COUNT(*) FROM BOOKING WHERE Status = 'FAILED') AS failedBookings,
                    (SELECT COUNT(*) FROM PASSENGER) AS totalPassengers,
                    (SELECT COALESCE(SUM(Amount), 0) FROM PAYMENT WHERE Payment_Status = 'SUCCESS') AS grossRevenue,
                    (SELECT COALESCE(SUM(Refund_Amount), 0) FROM CANCELLATION) AS totalRefunds,
                    ((SELECT COALESCE(SUM(Amount), 0) FROM PAYMENT WHERE Payment_Status IN ('SUCCESS', 'REFUNDED'))
                     - (SELECT COALESCE(SUM(Refund_Amount), 0) FROM CANCELLATION)) AS netRevenue
                """;
        return jdbc.queryForMap(sql);
    }

    public List<Map<String, Object>> getRevenueReport(LocalDate from, LocalDate to) {
        String sql = """
                SELECT DATE(b.Booked_At) AS day,
                       SUM(p.Amount) AS gross,
                       COALESCE(SUM(c.Refund_Amount), 0) AS refunds,
                       (SUM(p.Amount) - COALESCE(SUM(c.Refund_Amount), 0)) AS net
                FROM BOOKING b
                JOIN PAYMENT p ON p.Booking_ID = b.Booking_ID AND p.Payment_Status IN ('SUCCESS', 'REFUNDED')
                LEFT JOIN CANCELLATION c ON c.Booking_ID = b.Booking_ID
                WHERE DATE(b.Booked_At) BETWEEN ? AND ?
                GROUP BY DATE(b.Booked_At)
                ORDER BY day ASC
                """;
        return jdbc.queryForList(sql, from, to);
    }

    public List<Map<String, Object>> getPopularTrains(int limit) {
        String sql = """
                SELECT t.Train_ID AS trainId, t.Train_Number AS trainNumber, t.Train_Name AS trainName,
                       COUNT(b.Booking_ID) AS bookingCount,
                       COALESCE(SUM(b.Total_Fare), 0) AS totalRevenue
                FROM TRAIN t
                LEFT JOIN BOOKING b ON b.Train_ID = t.Train_ID AND b.Status = 'CONFIRMED'
                GROUP BY t.Train_ID, t.Train_Number, t.Train_Name
                ORDER BY bookingCount DESC, totalRevenue DESC
                LIMIT ?
                """;
        return jdbc.queryForList(sql, limit);
    }

    public List<Map<String, Object>> getOccupancyReport(LocalDate date) {
        String sql = """
                SELECT t.Train_ID AS trainId, t.Train_Number AS trainNumber, t.Train_Name AS trainName,
                       c.Class_Type AS classType,
                       COUNT(DISTINCT s.Seat_ID) AS totalSeats,
                       COUNT(DISTINCT sr.Reservation_ID) AS reservedSeats,
                       ROUND((COUNT(DISTINCT sr.Reservation_ID) / COUNT(DISTINCT s.Seat_ID)) * 100, 2) AS occupancyPercentage
                FROM TRAIN t
                JOIN COACH c ON c.Train_ID = t.Train_ID
                JOIN SEAT s ON s.Coach_ID = c.Coach_ID
                LEFT JOIN SEAT_RESERVATION sr ON sr.Seat_ID = s.Seat_ID
                                              AND sr.Journey_Date = ?
                                              AND sr.Status = 'ACTIVE'
                GROUP BY t.Train_ID, t.Train_Number, t.Train_Name, c.Class_Type
                ORDER BY t.Train_ID, c.Class_Type
                """;
        return jdbc.queryForList(sql, date);
    }

    public List<Map<String, Object>> executeLabQuery(String sql) {
        return jdbc.queryForList(sql);
    }
}
