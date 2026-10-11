package com.railway.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FareRepository {

    private final JdbcTemplate jdbc;

    public FareRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findByClassType(String classType) {
        String sql = "SELECT Class_Type, Rate_Per_Km, Base_Fare FROM CLASS_FARE WHERE Class_Type = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(sql, classType);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Map<String, Object>> findAll() {
        return jdbc.queryForList("SELECT Class_Type, Rate_Per_Km, Base_Fare FROM CLASS_FARE");
    }
}
