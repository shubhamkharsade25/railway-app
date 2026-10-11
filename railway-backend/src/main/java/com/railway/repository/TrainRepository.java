package com.railway.repository;

import com.railway.dto.RouteStopDto;
import com.railway.dto.TrainDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainRepository {

    private final JdbcTemplate jdbc;

    public TrainRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<TrainDto> trainMapper = (rs, rowNum) -> new TrainDto(
            rs.getInt("trainId"),
            rs.getString("trainNumber"),
            rs.getString("trainName"),
            rs.getString("trainType"),
            rs.getBoolean("isActive")
    );

    private final RowMapper<RouteStopDto> routeMapper = (rs, rowNum) -> {
        java.sql.Time arrival = rs.getTime("arrivalTime");
        java.sql.Time departure = rs.getTime("departureTime");
        return new RouteStopDto(
                rs.getInt("stopNo"),
                rs.getInt("stationId"),
                rs.getString("stationName"),
                rs.getString("stationCode"),
                arrival != null ? arrival.toLocalTime() : null,
                departure != null ? departure.toLocalTime() : null,
                rs.getInt("distanceKm")
        );
    };

    public List<TrainDto> findAllActive() {
        String sql = """
                SELECT Train_ID AS trainId, Train_Number AS trainNumber,
                       Train_Name AS trainName, Train_Type AS trainType, Is_Active AS isActive
                FROM TRAIN WHERE Is_Active = 1 ORDER BY Train_Number
                """;
        return jdbc.query(sql, trainMapper);
    }

    public Optional<TrainDto> findById(int trainId) {
        String sql = """
                SELECT Train_ID AS trainId, Train_Number AS trainNumber,
                       Train_Name AS trainName, Train_Type AS trainType, Is_Active AS isActive
                FROM TRAIN WHERE Train_ID = ?
                """;
        List<TrainDto> results = jdbc.query(sql, trainMapper, trainId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<RouteStopDto> findRoute(int trainId) {
        String sql = """
                SELECT ts.Stop_No AS stopNo, s.Station_ID AS stationId,
                       s.Station_Name AS stationName, s.Station_Code AS stationCode,
                       ts.Arrival_Time AS arrivalTime, ts.Departure_Time AS departureTime,
                       ts.Distance_Km AS distanceKm
                FROM TRAIN_STATION ts JOIN STATION s ON s.Station_ID = ts.Station_ID
                WHERE ts.Train_ID = ? ORDER BY ts.Stop_No
                """;
        return jdbc.query(sql, routeMapper, trainId);
    }

    public boolean isActive(int trainId) {
        String sql = "SELECT Is_Active FROM TRAIN WHERE Train_ID = ?";
        List<Integer> results = jdbc.query(sql, (rs, rn) -> rs.getInt("Is_Active"), trainId);
        return !results.isEmpty() && results.get(0) == 1;
    }

    public List<TrainDto> findAll() {
        String sql = """
                SELECT Train_ID AS trainId, Train_Number AS trainNumber,
                       Train_Name AS trainName, Train_Type AS trainType, Is_Active AS isActive
                FROM TRAIN ORDER BY Train_Number
                """;
        return jdbc.query(sql, trainMapper);
    }

    public int insertTrain(String trainNumber, String trainName, String trainType, boolean isActive) {
        String sql = "INSERT INTO TRAIN (Train_Number, Train_Name, Train_Type, Is_Active) VALUES (?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(con -> {
            java.sql.PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, trainNumber);
            ps.setString(2, trainName);
            ps.setString(3, trainType);
            ps.setInt(4, isActive ? 1 : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void updateTrain(int trainId, String trainName, String trainType, boolean isActive) {
        jdbc.update("UPDATE TRAIN SET Train_Name = ?, Train_Type = ?, Is_Active = ? WHERE Train_ID = ?",
                trainName, trainType, isActive ? 1 : 0, trainId);
    }

    public int countBookingsForTrain(int trainId) {
        String sql = "SELECT COUNT(*) FROM BOOKING WHERE Train_ID = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, trainId);
        return count != null ? count : 0;
    }

    public void deleteTrainCascade(int trainId) {
        // Seats cascade when coaches are deleted (via FK CASCADE)
        // 1. Delete coaches
        jdbc.update("DELETE FROM COACH WHERE Train_ID = ?", trainId);
        // 2. Delete route stops
        jdbc.update("DELETE FROM TRAIN_STATION WHERE Train_ID = ?", trainId);
        // 3. Delete train
        jdbc.update("DELETE FROM TRAIN WHERE Train_ID = ?", trainId);
    }

    public void insertRouteStop(int trainId, int stationId, int stopNo,
                                java.time.LocalTime arrivalTime, java.time.LocalTime departureTime, int distanceKm) {
        String sql = """
                INSERT INTO TRAIN_STATION (Train_ID, Station_ID, Stop_No, Arrival_Time, Departure_Time, Distance_Km)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql, trainId, stationId, stopNo,
                arrivalTime != null ? java.sql.Time.valueOf(arrivalTime) : null,
                departureTime != null ? java.sql.Time.valueOf(departureTime) : null,
                distanceKm);
    }

    public int insertCoach(int trainId, String coachNumber, String classType) {
        String sql = "INSERT INTO COACH (Train_ID, Coach_Number, Class_Type) VALUES (?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(con -> {
            java.sql.PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, trainId);
            ps.setString(2, coachNumber);
            ps.setString(3, classType);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void batchInsertSeats(int coachId, int seatCount) {
        String sql = "INSERT INTO SEAT (Coach_ID, Seat_Number, Seat_Type) VALUES (?, ?, ?)";
        List<Object[]> batchArgs = new java.util.ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            String seatType = switch (i % 8) {
                case 1, 5 -> "LOWER";
                case 2, 6 -> "MIDDLE";
                case 3, 7 -> "UPPER";
                case 4 -> "SIDE_LOWER";
                default -> "SIDE_UPPER";
            };
            batchArgs.add(new Object[]{coachId, i, seatType});
        }
        jdbc.batchUpdate(sql, batchArgs);
    }

    public void deleteRouteStop(int trainId, int stationId) {
        jdbc.update("DELETE FROM TRAIN_STATION WHERE Train_ID = ? AND Station_ID = ?", trainId, stationId);
    }

    public void deleteCoach(int coachId) {
        // SEAT rows cascade delete automatically because fk_seat_coach ON DELETE CASCADE
        jdbc.update("DELETE FROM COACH WHERE Coach_ID = ?", coachId);
    }
}
