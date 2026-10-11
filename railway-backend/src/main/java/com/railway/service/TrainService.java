package com.railway.service;

import com.railway.dto.RouteStopDto;
import com.railway.dto.TrainDto;
import com.railway.exception.ResourceNotFoundException;
import com.railway.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    public List<TrainDto> getAllActive() {
        return trainRepository.findAllActive();
    }

    public List<TrainDto> getAll(boolean includeInactive) {
        return includeInactive ? trainRepository.findAll() : trainRepository.findAllActive();
    }

    public TrainDto getById(int trainId) {
        return trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + trainId));
    }

    public List<RouteStopDto> getRoute(int trainId) {
        trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + trainId));
        return trainRepository.findRoute(trainId);
    }

    public TrainDto create(com.railway.dto.TrainRequest req) {
        int id = trainRepository.insertTrain(
                req.trainNumber().trim(),
                req.trainName().trim(),
                req.trainType().trim(),
                req.isActive()
        );
        return getById(id);
    }

    public TrainDto update(int trainId, com.railway.dto.TrainRequest req) {
        getById(trainId);
        trainRepository.updateTrain(trainId, req.trainName().trim(), req.trainType().trim(), req.isActive());
        return getById(trainId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(int trainId) {
        getById(trainId);
        int bookingCount = trainRepository.countBookingsForTrain(trainId);
        if (bookingCount > 0) {
            throw new com.railway.exception.ConflictException(
                    "Train has " + bookingCount + " bookings; deactivate instead of deleting");
        }
        trainRepository.deleteTrainCascade(trainId);
    }

    public void addRouteStop(int trainId, com.railway.dto.RouteStopRequest req, com.railway.repository.StationRepository stationRepository) {
        getById(trainId);
        if (!stationRepository.existsById(req.stationId())) {
            throw new ResourceNotFoundException("Station not found with ID: " + req.stationId());
        }

        List<RouteStopDto> existingRoute = trainRepository.findRoute(trainId);
        for (RouteStopDto stop : existingRoute) {
            if (stop.stationId().equals(req.stationId())) {
                throw new com.railway.exception.ConflictException("Station is already present on this route");
            }
            if (stop.stopNo().equals(req.stopNo())) {
                throw new com.railway.exception.ConflictException("Stop number " + req.stopNo() + " is already taken");
            }
        }

        if (req.arrivalTime() != null && req.departureTime() != null) {
            if (req.departureTime().isBefore(req.arrivalTime())) {
                throw new com.railway.exception.BadRequestException("Departure time cannot be before arrival time");
            }
        }

        trainRepository.insertRouteStop(trainId, req.stationId(), req.stopNo(),
                req.arrivalTime(), req.departureTime(), req.distanceKm());
    }

    @org.springframework.transaction.annotation.Transactional
    public void addCoachWithSeats(int trainId, com.railway.dto.CoachRequest req) {
        getById(trainId);
        int coachId = trainRepository.insertCoach(trainId, req.coachNumber().trim().toUpperCase(), req.classType());
        trainRepository.batchInsertSeats(coachId, req.seatCount());
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteRouteStop(int trainId, int stationId) {
        getById(trainId);
        trainRepository.deleteRouteStop(trainId, stationId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteCoach(int trainId, int coachId) {
        getById(trainId);
        trainRepository.deleteCoach(coachId);
    }
}
