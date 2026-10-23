package com.equations.equations_api.history.api;

import com.equations.equations_api.history.CalculationHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/history")
public class CalculationHistoryController {
    private final CalculationHistoryService historyService;

    public CalculationHistoryController(CalculationHistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalculationHistoryEntry save(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SaveCalculationRequest request) {
        return historyService.save(userId(jwt), request);
    }

    @GetMapping
    public List<CalculationHistoryEntry> findRecent(@AuthenticationPrincipal Jwt jwt) {
        return historyService.findRecent(userId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        if (!historyService.delete(id, userId(jwt))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved calculation was not found.");
        }
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
