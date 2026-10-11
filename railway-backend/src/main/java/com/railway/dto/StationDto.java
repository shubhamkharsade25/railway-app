package com.railway.dto;

import java.time.LocalTime;

public record StationDto(
        Integer stationId,
        String stationName,
        String stationCode,
        String city
) {}
