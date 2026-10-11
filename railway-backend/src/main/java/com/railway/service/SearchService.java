package com.railway.service;

import com.railway.dto.AvailabilityDto;
import com.railway.dto.TrainSearchResultDto;
import com.railway.exception.BadRequestException;
import com.railway.exception.ResourceNotFoundException;
import com.railway.repository.AvailabilityRepository;
import com.railway.repository.FareRepository;
import com.railway.repository.SearchRepository;
import com.railway.repository.StationRepository;
import com.railway.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SearchService {

    private static final Set<String> VALID_CLASSES = Set.of("SL", "3A", "2A", "1A", "CC");
    private static final int MAX_ADVANCE_DAYS = 120;

    private final SearchRepository searchRepository;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final AvailabilityRepository availabilityRepository;
    private final FareRepository fareRepository;

    public SearchService(SearchRepository searchRepository, StationRepository stationRepository,
                         TrainRepository trainRepository, AvailabilityRepository availabilityRepository,
                         FareRepository fareRepository) {
        this.searchRepository = searchRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.availabilityRepository = availabilityRepository;
        this.fareRepository = fareRepository;
    }

    /**
     * Search trains between two stations on a given date, optionally filtered by class.
     */
    public List<TrainSearchResultDto> searchTrains(int sourceId, int destinationId,
                                                    LocalDate date, String classType) {
        // Validate stations
        if (sourceId == destinationId) {
            throw new BadRequestException("Source and destination stations must be different");
        }
        if (!stationRepository.existsById(sourceId)) {
            throw new ResourceNotFoundException("Source station not found with ID: " + sourceId);
        }
        if (!stationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination station not found with ID: " + destinationId);
        }

        // Validate date
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BadRequestException("Journey date cannot be in the past");
        }
        if (date.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            throw new BadRequestException("Journey date cannot be more than " + MAX_ADVANCE_DAYS + " days in advance");
        }

        // Validate classType if provided
        if (classType != null && !VALID_CLASSES.contains(classType)) {
            throw new BadRequestException("Invalid class type. Must be one of: " + VALID_CLASSES);
        }

        // Execute core search
        List<SearchRepository.TrainSearchResult> results = searchRepository.searchTrains(sourceId, destinationId);

        // Build response with fare and availability per class
        List<String> classesToCheck = (classType != null)
                ? List.of(classType)
                : new ArrayList<>(VALID_CLASSES);

        List<TrainSearchResultDto> response = new ArrayList<>();
        for (SearchRepository.TrainSearchResult train : results) {
            List<TrainSearchResultDto.ClassAvailabilityDto> classAvailability = new ArrayList<>();
            for (String cls : classesToCheck) {
                if (availabilityRepository.classExistsOnTrain(train.trainId(), cls)) {
                    BigDecimal fare = calculateFare(cls, train.distanceKm());
                    int available = availabilityRepository.countAvailableSeats(train.trainId(), cls, date);
                    int total = availabilityRepository.countTotalSeats(train.trainId(), cls);
                    classAvailability.add(new TrainSearchResultDto.ClassAvailabilityDto(cls, fare, available, total));
                }
            }
            String duration = formatDuration(train.departureTime(), train.arrivalTime());
            response.add(new TrainSearchResultDto(
                    train.trainId(), train.trainNumber(), train.trainName(), train.trainType(),
                    train.departureTime(), train.arrivalTime(), train.distanceKm(),
                    duration, classAvailability
            ));
        }
        return response;
    }

    /**
     * Get seat availability for a specific train, class, and date.
     */
    public AvailabilityDto getAvailability(int trainId, String classType, LocalDate date, boolean includeSeats) {
        // Validate train exists
        trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + trainId));

        // Validate class
        if (!VALID_CLASSES.contains(classType)) {
            throw new BadRequestException("Invalid class type. Must be one of: " + VALID_CLASSES);
        }
        if (!availabilityRepository.classExistsOnTrain(trainId, classType)) {
            throw new BadRequestException("Class " + classType + " is not available on train " + trainId);
        }

        // Validate date
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BadRequestException("Journey date cannot be in the past");
        }

        int total = availabilityRepository.countTotalSeats(trainId, classType);
        int available = availabilityRepository.countAvailableSeats(trainId, classType, date);
        List<AvailabilityDto.SeatInfo> seats = includeSeats
                ? availabilityRepository.getAvailableSeats(trainId, classType, date)
                : null;

        return new AvailabilityDto(trainId, classType, date.toString(), total, available, seats);
    }

    /**
     * Calculate fare using BigDecimal: Base_Fare + Rate_Per_Km × distanceKm.
     */
    public BigDecimal calculateFare(String classType, int distanceKm) {
        Map<String, Object> fareRow = fareRepository.findByClassType(classType)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for class: " + classType));
        BigDecimal ratePerKm = new BigDecimal(fareRow.get("Rate_Per_Km").toString());
        BigDecimal baseFare = new BigDecimal(fareRow.get("Base_Fare").toString());
        return baseFare.add(ratePerKm.multiply(BigDecimal.valueOf(distanceKm))).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Format travel duration, handling overnight journeys (arrival before departure time).
     */
    private String formatDuration(LocalTime departure, LocalTime arrival) {
        if (departure == null || arrival == null) return "N/A";
        int depMinutes = departure.getHour() * 60 + departure.getMinute();
        int arrMinutes = arrival.getHour() * 60 + arrival.getMinute();
        int durationMinutes = arrMinutes - depMinutes;
        if (durationMinutes < 0) durationMinutes += 24 * 60; // overnight
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return hours + "h " + minutes + "m";
    }
}
