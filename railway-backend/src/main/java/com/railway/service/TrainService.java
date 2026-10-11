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

    public TrainDto getById(int trainId) {
        return trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + trainId));
    }

    public List<RouteStopDto> getRoute(int trainId) {
        // Ensure train exists
        trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID: " + trainId));
        return trainRepository.findRoute(trainId);
    }
}
