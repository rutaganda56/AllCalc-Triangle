package com.equations.equations_api.calculation.api;

import com.equations.equations_api.calculation.CalculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calculations")
public class CalculationController {
    private final CalculationService calculationService;

    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CalculationResponse calculate(
            @Valid @RequestBody CalculationRequest request,
            @RequestParam(required = false) List<String> sides) {
        return calculationService.calculate(request, sides);
    }
}
