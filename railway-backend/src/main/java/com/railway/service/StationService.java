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

    public StationDto create(com.railway.dto.StationRequest req) {
        int id = stationRepository.insert(req.stationName().trim(), req.stationCode().trim().toUpperCase(), req.city().trim());
        return getById(id);
    }

    public StationDto update(int stationId, com.railway.dto.StationRequest req) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Station not found with ID: " + stationId);
        }
        stationRepository.update(stationId, req.stationName().trim(), req.stationCode().trim().toUpperCase(), req.city().trim());
        return getById(stationId);
    }

    public void delete(int stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Station not found with ID: " + stationId);
        }
        if (stationRepository.isReferencedInRoute(stationId)) {
            throw new com.railway.exception.ConflictException("Cannot delete station as it is referenced in train routes");
        }
        stationRepository.delete(stationId);
    }
}
