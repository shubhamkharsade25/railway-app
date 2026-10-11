package com.railway.dto;

public record TrainDto(
        Integer trainId,
        String trainNumber,
        String trainName,
        String trainType,
        Boolean isActive
) {}
