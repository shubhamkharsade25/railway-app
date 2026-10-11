package com.railway.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancellationResponse(
        String pnr,
        String status,
        BigDecimal refundAmount,
        LocalDateTime cancelledAt
) {}
