package com.equations.equations_api.calculation.api;

import java.util.List;
import java.util.Map;

public record CalculationResponse(
        Vertices vertices,
        SideLengths sides,
        AngleMeasures angles,
        double area,
        double perimeter,
        TriangleCenters centers,
        Circle incircle,
        Circle circumcircle,
        TriangleLines lines,
        List<CalculationStep> steps) {

    public record Point(double x, double y) {
    }

    public record Vertices(Point a, Point b, Point c) {
    }

    public record SideLengths(double a, double b, double c) {
    }

    public record AngleMeasures(double a, double b, double c) {
    }

    public record TriangleCenters(Point centroid, Point incenter, Point orthocenter, Point circumcenter) {
    }

    public record Circle(Point center, double radius, double area, String equation) {
    }

    public record TriangleLines(Map<String, SideLineEquations> bySide) {
    }

    public record SideLineEquations(
            LineEquation side,
            String medianFromOppositeVertex,
            LineEquation median,
            double medianLength,
            String altitudeFromOppositeVertex,
            LineEquation altitude,
            double altitudeLength,
            LineEquation perpendicularBisector) {
    }

    public record LineEquation(
            String vector,
            String implicit,
            String parametric,
            String symmetric,
            String slopeIntercept,
            String normal,
            String polar,
            String interceptForm,
            Double xIntercept,
            Double yIntercept,
            double coefficientX,
            double coefficientY,
            double constant) {
    }

    public record CalculationStep(String name, String formula, String substitution, double result) {
    }
}
