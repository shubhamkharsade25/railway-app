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

    @PostMapping
    public ResponseEntity<StationDto> create(@jakarta.validation.Valid @RequestBody com.railway.dto.StationRequest req) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(stationService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationDto> update(
            @PathVariable int id,
            @jakarta.validation.Valid @RequestBody com.railway.dto.StationRequest req) {
        return ResponseEntity.ok(stationService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
