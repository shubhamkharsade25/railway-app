package com.railway.repository;

import com.railway.dto.StationDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StationRepository {

    private final JdbcTemplate jdbc;

    public StationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<StationDto> stationMapper = (rs, rowNum) -> new StationDto(
            rs.getInt("stationId"),
            rs.getString("stationName"),
            rs.getString("stationCode"),
            rs.getString("city")
    );

    public List<StationDto> findAll() {
        String sql = """
                SELECT Station_ID AS stationId, Station_Name AS stationName,
                       Station_Code AS stationCode, City AS city
                FROM STATION ORDER BY Station_Name
                """;
        return jdbc.query(sql, stationMapper);
    }

    public List<StationDto> search(String q) {
        String sql = """
                SELECT Station_ID AS stationId, Station_Name AS stationName,
                       Station_Code AS stationCode, City AS city
                FROM STATION
                WHERE Station_Name LIKE ? OR Station_Code LIKE ?
                ORDER BY Station_Name
                """;
        String param = "%" + q + "%";
        return jdbc.query(sql, stationMapper, param, param);
    }

    public Optional<StationDto> findById(int stationId) {
        String sql = """
                SELECT Station_ID AS stationId, Station_Name AS stationName,
                       Station_Code AS stationCode, City AS city
                FROM STATION WHERE Station_ID = ?
                """;
        List<StationDto> results = jdbc.query(sql, stationMapper, stationId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsById(int stationId) {
        String sql = "SELECT COUNT(*) FROM STATION WHERE Station_ID = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, stationId);
        return count != null && count > 0;
    }
}
