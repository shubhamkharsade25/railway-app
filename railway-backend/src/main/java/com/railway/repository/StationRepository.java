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

    public int insert(String name, String code, String city) {
        String sql = "INSERT INTO STATION (Station_Name, Station_Code, City) VALUES (?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(con -> {
            java.sql.PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, code);
            ps.setString(3, city);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(int stationId, String name, String code, String city) {
        jdbc.update("UPDATE STATION SET Station_Name = ?, Station_Code = ?, City = ? WHERE Station_ID = ?",
                name, code, city, stationId);
    }

    public boolean isReferencedInRoute(int stationId) {
        String sql = "SELECT COUNT(*) FROM TRAIN_STATION WHERE Station_ID = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, stationId);
        return count != null && count > 0;
    }

    public void delete(int stationId) {
        jdbc.update("DELETE FROM STATION WHERE Station_ID = ?", stationId);
    }
}
