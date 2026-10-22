package com.equations.equations_api.calculation.api;

import com.equations.equations_api.calculation.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalculationControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CalculationController(new CalculationService()))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void calculatesTriangleFromCoordinatesAndReturnsJson() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "x1": 0,
                                  "y1": 0,
                                  "x2": 3,
                                  "y2": 0,
                                  "x3": 0,
                                  "y3": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sides.a").value(5.0))
                .andExpect(jsonPath("$.angles.a").value(90.0))
                .andExpect(jsonPath("$.area").value(6.0))
                .andExpect(jsonPath("$.steps[0].formula").isNotEmpty())
                .andExpect(jsonPath("$.incircle.radius").value(1.0))
                .andExpect(jsonPath("$.circumcircle.radius").value(2.5))
                .andExpect(jsonPath("$.lines.bySide.AC.side.implicit").isNotEmpty())
                .andExpect(jsonPath("$.lines.bySide.AC.median.implicit").isNotEmpty())
                .andExpect(jsonPath("$.lines.bySide.AC.altitude.implicit").isNotEmpty())
                .andExpect(jsonPath("$.lines.bySide.AC.perpendicularBisector.implicit").isNotEmpty())
                .andExpect(jsonPath("$.lines.bySide.AB.side.implicit").isNotEmpty())
                .andExpect(jsonPath("$.lines.bySide.BC.side.implicit").isNotEmpty());
    }

    @Test
    void returnsOnlySideGroupsRequestedByQueryParameter() throws Exception {
        mockMvc.perform(post("/api/v1/calculations?sides=AB&sides=BC")
                        .contentType("application/json")
                        .content("""
                                {
                                  "x1": 0,
                                  "y1": 0,
                                  "x2": 3,
                                  "y2": 0,
                                  "x3": 0,
                                  "y3": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.bySide.AB.side").exists())
                .andExpect(jsonPath("$.lines.bySide.BC.side").exists())
                .andExpect(jsonPath("$.lines.bySide.AC").doesNotExist());
    }

    @Test
    void rejectsUnsupportedSideQueryParameter() throws Exception {
        mockMvc.perform(post("/api/v1/calculations?sides=CD")
                        .contentType("application/json")
                        .content("""
                                {
                                  "x1": 0,
                                  "y1": 0,
                                  "x2": 3,
                                  "y2": 0,
                                  "x3": 0,
                                  "y3": 4
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CALCULATION_REQUEST"));
    }

    @Test
    void returnsStructuredBadRequestForCollinearPoints() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "x1": 0,
                                  "y1": 0,
                                  "x2": 1,
                                  "y2": 1,
                                  "x3": 2,
                                  "y3": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CALCULATION_REQUEST"))
                .andExpect(jsonPath("$.message").value("The three points must not be collinear."));
    }

    @Test
    void returnsValidationErrorWhenCoordinateIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "x1": 0,
                                  "y1": 0,
                                  "x2": 3,
                                  "y2": 0,
                                  "x3": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.y3").isNotEmpty());
    }
}
