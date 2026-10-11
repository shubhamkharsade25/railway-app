package com.railway.controller;

import com.railway.config.QueryLabCatalog;
import com.railway.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(reportService.getSummary());
    }

    @GetMapping("/reports/revenue")
    public ResponseEntity<List<Map<String, Object>>> revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getRevenue(from, to));
    }

    @GetMapping("/reports/popular-trains")
    public ResponseEntity<List<Map<String, Object>>> popularTrains(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.getPopularTrains(limit));
    }

    @GetMapping("/reports/occupancy")
    public ResponseEntity<List<Map<String, Object>>> occupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getOccupancy(date));
    }

    @GetMapping("/query-lab")
    public ResponseEntity<List<QueryLabCatalog.LabQuery>> queryLabCatalog() {
        return ResponseEntity.ok(reportService.getQueryLabCatalog());
    }

    @GetMapping("/query-lab/{id}/result")
    public ResponseEntity<Map<String, Object>> executeLab(@PathVariable String id) {
        return ResponseEntity.ok(reportService.executeQueryLab(id));
    }
}
