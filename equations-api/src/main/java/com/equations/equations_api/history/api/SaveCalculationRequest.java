package com.equations.equations_api.history.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveCalculationRequest(
        @NotNull Integer x1,
        @NotNull Integer y1,
        @NotNull Integer x2,
        @NotNull Integer y2,
        @NotNull Integer x3,
        @NotNull Integer y3,
        @Size(max = 120) String title) {
}
