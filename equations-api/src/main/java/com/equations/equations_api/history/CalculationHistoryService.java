package com.equations.equations_api.history;

import com.equations.equations_api.calculation.CalculationService;
import com.equations.equations_api.calculation.api.CalculationRequest;
import com.equations.equations_api.history.api.CalculationHistoryEntry;
import com.equations.equations_api.history.api.SaveCalculationRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CalculationHistoryService {
    private final CalculationService calculationService;
    private final CalculationHistoryRepository repository;

    public CalculationHistoryService(
            CalculationService calculationService,
            CalculationHistoryRepository repository) {
        this.calculationService = calculationService;
        this.repository = repository;
    }

    public CalculationHistoryEntry save(UUID userId, SaveCalculationRequest request) {
        CalculationRequest coordinates = new CalculationRequest(
                request.x1(),
                request.y1(),
                request.x2(),
                request.y2(),
                request.x3(),
                request.y3());
        var calculation = calculationService.calculate(coordinates);
        String title = request.title() == null || request.title().isBlank()
                ? defaultTitle(request)
                : request.title().trim();
        return repository.save(userId, title, calculation, Instant.now());
    }

    public List<CalculationHistoryEntry> findRecent(UUID userId) {
        return repository.findRecentByUserId(userId);
    }

    public boolean delete(UUID id, UUID userId) {
        return repository.deleteByIdAndUserId(id, userId);
    }

    private String defaultTitle(SaveCalculationRequest request) {
        return "Triangle (" + request.x1() + ", " + request.y1() + "), ("
                + request.x2() + ", " + request.y2() + "), ("
                + request.x3() + ", " + request.y3() + ")";
    }
}
