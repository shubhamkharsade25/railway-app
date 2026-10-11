package com.railway.service;

import com.railway.dto.StationDto;
import com.railway.exception.ResourceNotFoundException;
import com.railway.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationDto> getAll(String q) {
        if (q != null && !q.isBlank()) {
            return stationRepository.search(q.trim());
        }
        return stationRepository.findAll();
    }

    public StationDto getById(int stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));
    }
}
