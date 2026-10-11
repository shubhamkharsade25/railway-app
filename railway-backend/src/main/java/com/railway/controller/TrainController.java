package com.railway.controller;

import com.railway.dto.RouteStopDto;
import com.railway.dto.TrainDto;
import com.railway.dto.TrainSearchResultDto;
import com.railway.dto.AvailabilityDto;
import com.railway.service.SearchService;
import com.railway.service.TrainService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainService trainService;
    private final SearchService searchService;

    public TrainController(TrainService trainService, SearchService searchService) {
        this.trainService = trainService;
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<TrainDto>> getAll() {
        return ResponseEntity.ok(trainService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainDto> getById(@PathVariable int id) {
        return ResponseEntity.ok(trainService.getById(id));
    }

    @GetMapping("/{id}/route")
    public ResponseEntity<List<RouteStopDto>> getRoute(@PathVariable int id) {
        return ResponseEntity.ok(trainService.getRoute(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrainSearchResultDto>> search(
            @RequestParam int source,
            @RequestParam int destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String classType) {
        return ResponseEntity.ok(searchService.searchTrains(source, destination, date, classType));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityDto> availability(
            @PathVariable int id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String classType,
            @RequestParam(defaultValue = "false") boolean includeSeats) {
        return ResponseEntity.ok(searchService.getAvailability(id, classType, date, includeSeats));
    }
}
