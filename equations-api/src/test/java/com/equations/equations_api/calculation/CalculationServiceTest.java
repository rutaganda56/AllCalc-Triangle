package com.equations.equations_api.calculation;

import com.equations.equations_api.calculation.api.CalculationRequest;
import com.equations.equations_api.calculation.api.CalculationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculationServiceTest {
    private final CalculationService service = new CalculationService();

    @Test
    void calculatesTriangleFromTheSixIntegerCoordinates() {
        CalculationResponse result = service.calculate(new CalculationRequest(0, 0, 3, 0, 0, 4));

        assertEquals(5.0, result.sides().a(), 1e-10);
        assertEquals(4.0, result.sides().b(), 1e-10);
        assertEquals(3.0, result.sides().c(), 1e-10);
        assertEquals(90.0, result.angles().a(), 1e-10);
        assertEquals(53.1301023542, result.angles().b(), 1e-8);
        assertEquals(36.8698976458, result.angles().c(), 1e-8);
        assertEquals(6.0, result.area(), 1e-10);
        assertEquals(12.0, result.perimeter(), 1e-10);
        assertEquals(1.0, result.centers().centroid().x(), 1e-10);
        assertEquals(4.0 / 3.0, result.centers().centroid().y(), 1e-10);
        assertEquals(1.0, result.incircle().radius(), 1e-10);
        assertEquals(Math.PI, result.incircle().area(), 1e-10);
        assertEquals(2.5, result.circumcircle().radius(), 1e-10);
        assertEquals(6.25 * Math.PI, result.circumcircle().area(), 1e-10);
        assertEquals(0.0, result.centers().orthocenter().x(), 1e-10);
        assertEquals(0.0, result.centers().orthocenter().y(), 1e-10);
        assertEquals(1.5, result.centers().circumcenter().x(), 1e-10);
        assertEquals(2.0, result.centers().circumcenter().y(), 1e-10);
        assertEquals(Math.sqrt(13.0), result.lines().bySide().get("AC").medianLength(), 1e-10);
        assertEquals(3.0, result.lines().bySide().get("AC").altitudeLength(), 1e-10);
        assertTrue(result.lines().bySide().get("AC").side().implicit().contains("x"));
        assertTrue(result.lines().bySide().get("AC").median().parametric().contains("t"));
        assertTrue(result.lines().bySide().get("AC").altitude().normal().contains("= 0"));
        assertTrue(result.lines().bySide().get("AC").perpendicularBisector().implicit().contains("= 0"));
        assertLinePassesThrough(result.lines().bySide().get("AB").side(), 0.0, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").side(), 3.0, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").side(), 3.0, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").side(), 0.0, 4.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").median(), 0.0, 4.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").median(), 1.5, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").median(), 0.0, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").median(), 1.5, 2.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").altitude(), 0.0, 4.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").altitude(), 0.0, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").perpendicularBisector(), 1.5, 0.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").perpendicularBisector(), 1.5, 2.0);
        assertEquals(2.0 * result.area() / result.sides().c(),
                result.lines().bySide().get("AB").altitudeLength(), 1e-10);
        assertEquals(2.0 * result.area() / result.sides().a(),
                result.lines().bySide().get("BC").altitudeLength(), 1e-10);
        assertTrue(result.steps().stream().anyMatch(step -> step.name().equals("Angle A")));
        assertTrue(result.steps().stream().anyMatch(step -> step.name().equals("Area")));
    }

    @Test
    void calculatesReferenceTriangleMetricsAndAbLine() {
        CalculationResponse result = service.calculate(new CalculationRequest(-3, 2, 1, 5, 5, -7));

        assertEquals(5.0, result.sides().c(), 1e-10);
        assertEquals(Math.sqrt(145.0), result.sides().b(), 1e-10);
        assertEquals(Math.sqrt(160.0), result.sides().a(), 1e-10);
        assertEquals(30.0, result.area(), 1e-10);
        assertEquals(5.0 + Math.sqrt(145.0) + Math.sqrt(160.0), result.perimeter(), 1e-10);
        assertEquals(1.0, result.centers().centroid().x(), 1e-10);
        assertEquals(0.0, result.centers().centroid().y(), 1e-10);
        assertEquals(-2.0, result.centers().orthocenter().x(), 1e-10);
        assertEquals(7.0 / 3.0, result.centers().orthocenter().y(), 1e-10);
        assertEquals(2.5, result.centers().circumcenter().x(), 1e-10);
        assertEquals(-7.0 / 6.0, result.centers().circumcenter().y(), 1e-10);
        assertEquals(60.0 / result.perimeter(), result.incircle().radius(), 1e-10);
        assertEquals(5.0 * Math.sqrt(58.0) / 6.0, result.circumcircle().radius(), 1e-10);

        var ab = result.lines().bySide().get("AB").side();
        assertLinePassesThrough(ab, -3.0, 2.0);
        assertLinePassesThrough(ab, 1.0, 5.0);
        assertEquals(3.0, ab.coefficientX(), 1e-10);
        assertEquals(-4.0, ab.coefficientY(), 1e-10);
        assertEquals(17.0, ab.constant(), 1e-10);
        assertEquals(0.75, -ab.coefficientX() / ab.coefficientY(), 1e-10);
    }

    @Test
    void calculatesGeneralTriangleCentersAndTheAcPerpendicularBisector() {
        CalculationResponse result = service.calculate(new CalculationRequest(1, 2, 5, 3, 2, 7));

        assertEquals(8.0 / 3.0, result.centers().centroid().x(), 1e-10);
        assertEquals(4.0, result.centers().centroid().y(), 1e-10);
        assertEquals(180.0,
                result.angles().a() + result.angles().b() + result.angles().c(),
                1e-10);
        assertEquals(
                result.circumcircle().radius(),
                Math.hypot(result.centers().circumcenter().x() - 1.0,
                        result.centers().circumcenter().y() - 2.0),
                1e-10);
        assertEquals(
                result.circumcircle().radius(),
                Math.hypot(result.centers().circumcenter().x() - 2.0,
                        result.centers().circumcenter().y() - 7.0),
                1e-10);
        assertEquals(
                result.circumcircle().radius(),
                Math.hypot(result.centers().circumcenter().x() - 5.0,
                        result.centers().circumcenter().y() - 3.0),
                1e-10);
        assertEquals(4.8, result.lines().bySide().get("AC").perpendicularBisector().yIntercept(), 1e-10);
        assertEquals(Math.sqrt(14.5), result.lines().bySide().get("AC").medianLength(), 1e-10);
        assertEquals(19.0 / Math.sqrt(26.0), result.lines().bySide().get("AC").altitudeLength(), 1e-10);
        assertEquals(2.0 * result.area() / result.sides().c(),
                result.lines().bySide().get("AB").altitudeLength(), 1e-10);
        assertEquals(2.0 * result.area() / result.sides().a(),
                result.lines().bySide().get("BC").altitudeLength(), 1e-10);
        assertLinePassesThrough(result.lines().bySide().get("AB").side(), 1.0, 2.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").side(), 5.0, 3.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").side(), 5.0, 3.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").side(), 2.0, 7.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").median(), 2.0, 7.0);
        assertLinePassesThrough(result.lines().bySide().get("AB").median(), 3.0, 2.5);
        assertLinePassesThrough(result.lines().bySide().get("BC").median(), 1.0, 2.0);
        assertLinePassesThrough(result.lines().bySide().get("BC").median(), 3.5, 5.0);
        assertEquals(0.6, result.lines().bySide().get("AC").side().xIntercept(), 1e-10);
        assertEquals(-3.0, result.lines().bySide().get("AC").side().yIntercept(), 1e-10);
        assertEquals("x / 0.6 + y / -3.0 = 1", result.lines().bySide().get("AC").side().interceptForm());
        assertEquals(
                "r * (5.0 * cos(theta) + -1.0 * sin(theta)) + -3.0 = 0",
                result.lines().bySide().get("AC").side().polar());
        assertTrue(result.circumcircle().area() > 0.0);
    }

    @Test
    void calculatesAreaRegardlessOfPointOrdering() {
        CalculationResponse result = service.calculate(new CalculationRequest(0, 4, 3, 0, 0, 0));

        assertEquals(6.0, result.area(), 1e-10);
        assertEquals(12.0, result.perimeter(), 1e-10);
        assertEquals("Undefined: intercept form requires two nonzero axis intercepts.",
                result.lines().bySide().get("AC").side().interceptForm());
        assertEquals("r * (-4.0 * cos(theta) + 0.0 * sin(theta)) + 0.0 = 0",
                result.lines().bySide().get("AC").side().polar());
    }

    @Test
    void rejectsRepeatedPoints() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(new CalculationRequest(1, 1, 1, 1, 2, 3)));

        assertTrue(exception.getMessage().contains("distinct"));
    }

    @Test
    void rejectsCollinearPoints() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(new CalculationRequest(0, 0, 1, 1, 2, 2)));

        assertTrue(exception.getMessage().contains("collinear"));
    }

    @Test
    void rejectsMissingCoordinates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(new CalculationRequest(0, 0, 3, 0, 0, null)));
    }

    @Test
    void selectsOnlyRequestedSideEquationGroups() {
        CalculationResponse result = service.calculate(
                new CalculationRequest(0, 0, 3, 0, 0, 4),
                List.of("bc", "AB"));

        assertEquals(List.of("BC", "AB"), List.copyOf(result.lines().bySide().keySet()));
    }

    @Test
    void rejectsUnsupportedSideSelection() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(new CalculationRequest(0, 0, 3, 0, 0, 4), List.of("CD")));

        assertTrue(exception.getMessage().contains("Choose AC, AB, or BC"));
    }

    private void assertLinePassesThrough(
            CalculationResponse.LineEquation line, double x, double y) {
        assertEquals(
                0.0,
                line.coefficientX() * x + line.coefficientY() * y + line.constant(),
                1e-10);
    }
}
