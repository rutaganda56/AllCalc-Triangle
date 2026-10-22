package com.equations.equations_api.calculation.api;

import jakarta.validation.constraints.NotNull;

public record CalculationRequest(
        @NotNull Integer x1,
        @NotNull Integer y1,
        @NotNull Integer x2,
        @NotNull Integer y2,
        @NotNull Integer x3,
        @NotNull Integer y3) {
}
