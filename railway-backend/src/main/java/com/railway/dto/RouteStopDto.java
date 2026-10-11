package com.railway.dto;

import java.time.LocalTime;

public record RouteStopDto(
        Integer stopNo,
        Integer stationId,
        String stationName,
        String stationCode,
        LocalTime arrivalTime,
        LocalTime departureTime,
        Integer distanceKm
) {}
