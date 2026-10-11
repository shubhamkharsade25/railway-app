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
    private final com.railway.repository.StationRepository stationRepository;

    public TrainController(TrainService trainService, SearchService searchService, com.railway.repository.StationRepository stationRepository) {
        this.trainService = trainService;
        this.searchService = searchService;
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public ResponseEntity<List<TrainDto>> getAll(@RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(trainService.getAll(all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainDto> getById(@PathVariable int id) {
        return ResponseEntity.ok(trainService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TrainDto> create(@jakarta.validation.Valid @RequestBody com.railway.dto.TrainRequest req) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(trainService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainDto> update(
            @PathVariable int id,
            @jakarta.validation.Valid @RequestBody com.railway.dto.TrainRequest req) {
        return ResponseEntity.ok(trainService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        trainService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/route/stops")
    public ResponseEntity<Void> addRouteStop(
            @PathVariable int id,
            @jakarta.validation.Valid @RequestBody com.railway.dto.RouteStopRequest req) {
        trainService.addRouteStop(id, req, stationRepository);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/route/stops/{stationId}")
    public ResponseEntity<Void> deleteRouteStop(@PathVariable int id, @PathVariable int stationId) {
        trainService.deleteRouteStop(id, stationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/coaches")
    public ResponseEntity<Void> addCoach(
            @PathVariable int id,
            @jakarta.validation.Valid @RequestBody com.railway.dto.CoachRequest req) {
        trainService.addCoachWithSeats(id, req);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/coaches/{coachId}")
    public ResponseEntity<Void> deleteCoach(@PathVariable int id, @PathVariable int coachId) {
        trainService.deleteCoach(id, coachId);
        return ResponseEntity.noContent().build();
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
