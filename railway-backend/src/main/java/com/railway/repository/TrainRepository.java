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
}
