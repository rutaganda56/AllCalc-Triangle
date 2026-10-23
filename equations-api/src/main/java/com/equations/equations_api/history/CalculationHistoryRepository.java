package com.equations.equations_api.history;

import com.equations.equations_api.calculation.api.CalculationResponse;
import com.equations.equations_api.history.api.CalculationHistoryEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class CalculationHistoryRepository {
    private static final int HISTORY_LIMIT = 100;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CalculationHistoryRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public CalculationHistoryEntry save(
            UUID userId,
            String title,
            CalculationResponse calculation,
            Instant createdAt) {
        UUID id = UUID.randomUUID();
        String calculationJson = serialize(calculation);
        jdbcTemplate.update(
                """
                        INSERT INTO calculation_history (id, user_id, title, calculation_json, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                id,
                userId,
                title,
                calculationJson,
                Timestamp.from(createdAt));
        return new CalculationHistoryEntry(id, title, calculation, createdAt);
    }

    public List<CalculationHistoryEntry> findRecentByUserId(UUID userId) {
        return jdbcTemplate.query(
                """
                        SELECT id, title, calculation_json, created_at
                        FROM calculation_history
                        WHERE user_id = ?
                        ORDER BY created_at DESC, id DESC
                        LIMIT ?
                        """,
                (resultSet, rowNumber) -> new CalculationHistoryEntry(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("title"),
                        deserialize(resultSet.getString("calculation_json")),
                        resultSet.getTimestamp("created_at").toInstant()),
                userId,
                HISTORY_LIMIT);
    }

    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        return jdbcTemplate.update(
                "DELETE FROM calculation_history WHERE id = ? AND user_id = ?",
                id,
                userId) == 1;
    }

    private String serialize(CalculationResponse calculation) {
        try {
            return objectMapper.writeValueAsString(calculation);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize the calculation for history.", exception);
        }
    }

    private CalculationResponse deserialize(String calculationJson) {
        try {
            return objectMapper.readValue(calculationJson, CalculationResponse.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not read a saved calculation from history.", exception);
        }
    }
}
