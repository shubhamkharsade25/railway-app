package com.railway.service;

import com.railway.config.QueryLabCatalog;
import com.railway.exception.ResourceNotFoundException;
import com.railway.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Map<String, Object> getSummary() {
        return reportRepository.getSystemSummary();
    }

    public List<Map<String, Object>> getRevenue(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return reportRepository.getRevenueReport(from, to);
    }

    public List<Map<String, Object>> getPopularTrains(int limit) {
        if (limit <= 0) limit = 5;
        return reportRepository.getPopularTrains(limit);
    }

    public List<Map<String, Object>> getOccupancy(LocalDate date) {
        if (date == null) date = LocalDate.now();
        return reportRepository.getOccupancyReport(date);
    }

    public List<QueryLabCatalog.LabQuery> getQueryLabCatalog() {
        return QueryLabCatalog.getAll();
    }

    public Map<String, Object> executeQueryLab(String queryId) {
        QueryLabCatalog.LabQuery labQuery = QueryLabCatalog.get(queryId);
        if (labQuery == null) {
            throw new ResourceNotFoundException("Query Lab definition not found for ID: " + queryId);
        }

        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = reportRepository.executeLabQuery(labQuery.sql());
        long duration = System.currentTimeMillis() - start;

        List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());

        return Map.of(
                "queryId", labQuery.id(),
                "title", labQuery.title(),
                "concept", labQuery.concept(),
                "sql", labQuery.sql(),
                "columns", columns,
                "rows", rows,
                "rowCount", rows.size(),
                "executionMillis", duration
        );
    }
}
