package com.railway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancellationRequest(
        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {}
