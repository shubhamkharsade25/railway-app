package com.railway.repository;

import com.railway.dto.AvailabilityDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AvailabilityRepository {

    private final JdbcTemplate jdbc;

    public AvailabilityRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Count available seats for a given train, class, and date.
     */
    public int countAvailableSeats(int trainId, String classType, LocalDate journeyDate) {
        String sql = """
                SELECT COUNT(*) FROM SEAT s
                JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE c.Train_ID = ? AND c.Class_Type = ?
                  AND NOT EXISTS (
                    SELECT 1 FROM SEAT_RESERVATION sr
                    WHERE sr.Seat_ID = s.Seat_ID AND sr.Journey_Date = ? AND sr.Status = 'ACTIVE')
                """;
        Integer count = jdbc.queryForObject(sql, Integer.class, trainId, classType, journeyDate);
        return count != null ? count : 0;
    }

    /**
     * Count total seats for a given train and class.
     */
    public int countTotalSeats(int trainId, String classType) {
        String sql = """
                SELECT COUNT(*) FROM SEAT s
                JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE c.Train_ID = ? AND c.Class_Type = ?
                """;
        Integer count = jdbc.queryForObject(sql, Integer.class, trainId, classType);
        return count != null ? count : 0;
    }

    /**
     * Get available seat list (for seat picker).
     */
    public List<AvailabilityDto.SeatInfo> getAvailableSeats(int trainId, String classType, LocalDate journeyDate) {
        String sql = """
                SELECT s.Seat_ID AS seatId, c.Coach_Number AS coachNumber,
                       s.Seat_Number AS seatNumber, s.Seat_Type AS seatType
                FROM SEAT s
                JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE c.Train_ID = ? AND c.Class_Type = ?
                  AND NOT EXISTS (
                    SELECT 1 FROM SEAT_RESERVATION sr
                    WHERE sr.Seat_ID = s.Seat_ID AND sr.Journey_Date = ? AND sr.Status = 'ACTIVE')
                ORDER BY c.Coach_Number, s.Seat_Number
                """;
        return jdbc.query(sql, (rs, rn) -> new AvailabilityDto.SeatInfo(
                rs.getInt("seatId"),
                rs.getString("coachNumber"),
                rs.getInt("seatNumber"),
                rs.getString("seatType")
        ), trainId, classType, journeyDate);
    }

    /**
     * Checks if the given class type exists on the train (i.e. there are coaches of that class).
     */
    public boolean classExistsOnTrain(int trainId, String classType) {
        String sql = "SELECT COUNT(*) FROM COACH WHERE Train_ID = ? AND Class_Type = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, trainId, classType);
        return count != null && count > 0;
    }

    /**
     * Get coach/seat details by seat ID.
     */
    public java.util.Map<String, Object> getSeatDetails(int seatId) {
        String sql = """
                SELECT s.Seat_Number AS Seat_Number, s.Seat_Type AS Seat_Type,
                       c.Coach_Number AS Coach_Number
                FROM SEAT s JOIN COACH c ON c.Coach_ID = s.Coach_ID
                WHERE s.Seat_ID = ?
                """;
        java.util.List<java.util.Map<String, Object>> rows = jdbc.queryForList(sql, seatId);
        return rows.isEmpty() ? java.util.Map.of() : rows.get(0);
    }
}
