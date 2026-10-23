package com.equations.equations_api.history.api;

import com.equations.equations_api.calculation.api.CalculationResponse;

import java.time.Instant;
import java.util.UUID;

public record CalculationHistoryEntry(
        UUID id,
        String title,
        CalculationResponse calculation,
        Instant createdAt) {
}
