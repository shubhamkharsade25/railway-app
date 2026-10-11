package com.railway.repository;

import com.railway.dto.TrainSearchResultDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchRepository {

    private final JdbcTemplate jdbc;

    public SearchRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Core search: finds trains that have source station BEFORE destination station.
     * Returns basic train info + departure/arrival times + distance between stops.
     */
    public List<TrainSearchResult> searchTrains(int sourceStationId, int destinationStationId) {
        String sql = """
                SELECT t.Train_ID AS trainId, t.Train_Number AS trainNumber,
                       t.Train_Name AS trainName, t.Train_Type AS trainType,
                       src.Departure_Time AS departureTime,
                       dst.Arrival_Time AS arrivalTime,
                       (dst.Distance_Km - src.Distance_Km) AS distanceKm
                FROM TRAIN t
                JOIN TRAIN_STATION src ON src.Train_ID = t.Train_ID AND src.Station_ID = ?
                JOIN TRAIN_STATION dst ON dst.Train_ID = t.Train_ID AND dst.Station_ID = ?
                WHERE src.Stop_No < dst.Stop_No AND t.Is_Active = 1
                ORDER BY src.Departure_Time
                """;
        return jdbc.query(sql, (rs, rowNum) -> new TrainSearchResult(
                rs.getInt("trainId"),
                rs.getString("trainNumber"),
                rs.getString("trainName"),
                rs.getString("trainType"),
                rs.getTime("departureTime") != null ? rs.getTime("departureTime").toLocalTime() : null,
                rs.getTime("arrivalTime") != null ? rs.getTime("arrivalTime").toLocalTime() : null,
                rs.getInt("distanceKm")
        ), sourceStationId, destinationStationId);
    }

    /**
     * Internal result holder for search query data.
     */
    public record TrainSearchResult(
            int trainId,
            String trainNumber,
            String trainName,
            String trainType,
            java.time.LocalTime departureTime,
            java.time.LocalTime arrivalTime,
            int distanceKm
    ) {}
}
