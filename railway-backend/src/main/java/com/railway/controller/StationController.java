package com.railway.controller;

import com.railway.dto.StationDto;
import com.railway.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<StationDto>> getAll(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(stationService.getAll(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationDto> getById(@PathVariable int id) {
        return ResponseEntity.ok(stationService.getById(id));
    }
}
