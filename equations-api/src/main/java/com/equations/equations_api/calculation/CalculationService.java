package com.equations.equations_api.calculation;

import com.equations.equations_api.calculation.api.CalculationRequest;
import com.equations.equations_api.calculation.api.CalculationResponse;
import com.equations.equations_api.calculation.api.CalculationResponse.AngleMeasures;
import com.equations.equations_api.calculation.api.CalculationResponse.CalculationStep;
import com.equations.equations_api.calculation.api.CalculationResponse.Circle;
import com.equations.equations_api.calculation.api.CalculationResponse.LineEquation;
import com.equations.equations_api.calculation.api.CalculationResponse.Point;
import com.equations.equations_api.calculation.api.CalculationResponse.SideLengths;
import com.equations.equations_api.calculation.api.CalculationResponse.SideLineEquations;
import com.equations.equations_api.calculation.api.CalculationResponse.TriangleCenters;
import com.equations.equations_api.calculation.api.CalculationResponse.TriangleLines;
import com.equations.equations_api.calculation.api.CalculationResponse.Vertices;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CalculationService {
    public CalculationResponse calculate(CalculationRequest request) {
        return calculate(request, null);
    }

    public CalculationResponse calculate(CalculationRequest request, List<String> requestedSides) {
        if (request == null
                || request.x1() == null || request.y1() == null
                || request.x2() == null || request.y2() == null
                || request.x3() == null || request.y3() == null) {
            throw new IllegalArgumentException("All six point coordinates are required.");
        }

        double x1 = request.x1();
        double y1 = request.y1();
        double x2 = request.x2();
        double y2 = request.y2();
        double x3 = request.x3();
        double y3 = request.y3();

        double a = distance(x2, y2, x3, y3);
        double b = distance(x1, y1, x3, y3);
        double c = distance(x1, y1, x2, y2);
        if (a == 0.0 || b == 0.0 || c == 0.0) {
            throw new IllegalArgumentException("The three points must be distinct.");
        }

        BigInteger abX = BigInteger.valueOf((long) request.x2() - request.x1());
        BigInteger abY = BigInteger.valueOf((long) request.y2() - request.y1());
        BigInteger acX = BigInteger.valueOf((long) request.x3() - request.x1());
        BigInteger acY = BigInteger.valueOf((long) request.y3() - request.y1());
        BigInteger determinant = abX.multiply(acY).subtract(abY.multiply(acX)).abs();
        double area = determinant.doubleValue() / 2.0;
        if (area == 0.0) {
            throw new IllegalArgumentException("The three points must not be collinear.");
        }

        List<CalculationStep> steps = new ArrayList<>();
        addStep(steps, "Side a", "a = distance(B, C)",
                "sqrt((" + x3 + " - " + x2 + ")^2 + (" + y3 + " - " + y2 + ")^2)", a);
        addStep(steps, "Side b", "b = distance(A, C)",
                "sqrt((" + x3 + " - " + x1 + ")^2 + (" + y3 + " - " + y1 + ")^2)", b);
        addStep(steps, "Side c", "c = distance(A, B)",
                "sqrt((" + x2 + " - " + x1 + ")^2 + (" + y2 + " - " + y1 + ")^2)", c);

        AngleMeasures angles = calculateAngles(a, b, c, steps);
        addStep(steps, "Area", "area = |(x2-x1)(y3-y1) - (y2-y1)(x3-x1)| / 2",
                "|(" + request.x2() + "-" + request.x1() + ")*(" + request.y3() + "-" + request.y1()
                        + ") - (" + request.y2() + "-" + request.y1() + ")*(" + request.x3() + "-"
                        + request.x1() + ")| / 2", area);

        double perimeter = a + b + c;
        Point pointA = new Point(x1, y1);
        Point pointB = new Point(x2, y2);
        Point pointC = new Point(x3, y3);
        Vertices vertices = new Vertices(pointA, pointB, pointC);
        TriangleCenters centers = calculateCenters(pointA, pointB, pointC, a, b, c, perimeter);
        double inradius = (2.0 * area) / perimeter;
        double circumradius = distance(centers.circumcenter().x(), centers.circumcenter().y(), x1, y1);
        Circle incircle = circle(centers.incenter(), inradius);
        Circle circumcircle = circle(centers.circumcenter(), circumradius);
        TriangleLines lines = calculateLines(pointA, pointB, pointC, area, a, b, c, requestedSides);
        addLineSteps(steps, lines);
        addStep(steps, "Centroid x", "Gx = (x1 + x2 + x3) / 3",
                "(" + x1 + " + " + x2 + " + " + x3 + ") / 3", centers.centroid().x());
        addStep(steps, "Centroid y", "Gy = (y1 + y2 + y3) / 3",
                "(" + y1 + " + " + y2 + " + " + y3 + ") / 3", centers.centroid().y());
        addStep(steps, "Inradius", "r = 2 * area / perimeter",
                "2 * " + area + " / " + perimeter, inradius);
        addStep(steps, "Circumradius", "R = a * b * c / (4 * area)",
                a + " * " + b + " * " + c + " / (4 * " + area + ")", circumradius);
        addStep(steps, "Incircle area", "area = pi * r^2",
                "pi * " + inradius + "^2", incircle.area());
        addStep(steps, "Circumcircle area", "area = pi * R^2",
                "pi * " + circumradius + "^2", circumcircle.area());
        addStep(steps, "Incircle equation", "(x - Ix)^2 + (y - Iy)^2 = r^2",
                incircle.equation(), inradius * inradius);
        addStep(steps, "Circumcircle equation", "(x - Ox)^2 + (y - Oy)^2 = R^2",
                circumcircle.equation(), circumradius * circumradius);
        addStep(steps, "Perimeter", "perimeter = a + b + c",
                a + " + " + b + " + " + c, perimeter);
        return new CalculationResponse(
                vertices,
                new SideLengths(a, b, c),
                angles,
                area,
                perimeter,
                centers,
                incircle,
                circumcircle,
                lines,
                List.copyOf(steps));
    }

    private TriangleCenters calculateCenters(
            Point a, Point b, Point c, double sideA, double sideB, double sideC, double perimeter) {
        Point centroid = new Point(
                (a.x() + b.x() + c.x()) / 3.0,
                (a.y() + b.y() + c.y()) / 3.0);
        Point incenter = new Point(
                (sideA * a.x() + sideB * b.x() + sideC * c.x()) / perimeter,
                (sideA * a.y() + sideB * b.y() + sideC * c.y()) / perimeter);

        double abX = b.x() - a.x();
        double abY = b.y() - a.y();
        double acX = c.x() - a.x();
        double acY = c.y() - a.y();
        double cross = abX * acY - abY * acX;
        double abSquared = abX * abX + abY * abY;
        double acSquared = acX * acX + acY * acY;
        double offsetX = (abSquared * acY - acSquared * abY) / (2.0 * cross);
        double offsetY = (abX * acSquared - acX * abSquared) / (2.0 * cross);
        Point circumcenter = new Point(a.x() + offsetX, a.y() + offsetY);
        Point orthocenter = new Point(
                a.x() + b.x() + c.x() - 2.0 * circumcenter.x(),
                a.y() + b.y() + c.y() - 2.0 * circumcenter.y());
        return new TriangleCenters(centroid, incenter, orthocenter, circumcenter);
    }

    private Circle circle(Point center, double radius) {
        double circleArea = Math.PI * radius * radius;
        ensureFinite(radius, "The coordinates produce a circle outside the supported numeric range.");
        ensureFinite(circleArea, "The coordinates produce a circle outside the supported numeric range.");
        String equation = "(x - " + center.x() + ")^2 + (y - " + center.y() + ")^2 = "
                + (radius * radius);
        return new Circle(center, radius, circleArea, equation);
    }

    private TriangleLines calculateLines(
            Point a, Point b, Point c, double area, double sideBC, double sideAC,
            double sideAB, List<String> requestedSides) {
        Point midpointAC = midpoint(a, c);
        Point midpointAB = midpoint(a, b);
        Point midpointBC = midpoint(b, c);
        Map<String, SideLineEquations> allSides = new LinkedHashMap<>();
        allSides.put("AC", sideEquations(
                a, c, b, midpointAC, area, sideAC, "B"));
        allSides.put("AB", sideEquations(
                a, b, c, midpointAB, area, sideAB, "C"));
        allSides.put("BC", sideEquations(
                b, c, a, midpointBC, area, sideBC, "A"));

        Collection<String> selection = requestedSides == null || requestedSides.isEmpty()
                ? allSides.keySet()
                : requestedSides;
        Map<String, SideLineEquations> selected = new LinkedHashMap<>();
        for (String requestedSide : selection) {
            if (requestedSide == null) {
                throw new IllegalArgumentException("Requested side values must be AC, AB, or BC.");
            }
            String side = requestedSide.trim().toUpperCase(Locale.ROOT);
            SideLineEquations equations = allSides.get(side);
            if (equations == null) {
                throw new IllegalArgumentException(
                        "Unsupported side '" + requestedSide + "'. Choose AC, AB, or BC.");
            }
            selected.putIfAbsent(side, equations);
        }
        return new TriangleLines(Collections.unmodifiableMap(selected));
    }

    private SideLineEquations sideEquations(
            Point first,
            Point second,
            Point opposite,
            Point midpoint,
            double area,
            double sideLength,
            String oppositeVertex) {
        LineEquation side = lineThrough(first, second);
        LineEquation median = lineThrough(opposite, midpoint);
        Point altitudeDirection = new Point(
                opposite.x() - (second.y() - first.y()),
                opposite.y() + (second.x() - first.x()));
        LineEquation altitude = lineThrough(opposite, altitudeDirection);
        double medianLength = distance(opposite.x(), opposite.y(), midpoint.x(), midpoint.y());
        double altitudeLength = 2.0 * area / sideLength;
        return new SideLineEquations(
                side,
                oppositeVertex,
                median,
                medianLength,
                oppositeVertex,
                altitude,
                altitudeLength,
                perpendicularBisector(first, second, midpoint));
    }

    private void addLineSteps(List<CalculationStep> steps, TriangleLines triangleLines) {
        for (Map.Entry<String, SideLineEquations> entry : triangleLines.bySide().entrySet()) {
            String side = entry.getKey();
            SideLineEquations equations = entry.getValue();
            addStep(steps, "Median from " + equations.medianFromOppositeVertex()
                            + " to " + side + " length",
                    "median = distance(opposite vertex, midpoint(" + side + "))",
                    equations.median().parametric(), equations.medianLength());
            addStep(steps, "Altitude from " + equations.altitudeFromOppositeVertex()
                            + " to " + side + " length",
                    "altitude = 2 * area / length(" + side + ")",
                    equations.altitude().implicit(), equations.altitudeLength());
        }
    }

    private Point midpoint(Point first, Point second) {
        return new Point((first.x() + second.x()) / 2.0, (first.y() + second.y()) / 2.0);
    }

    private LineEquation perpendicularBisector(Point first, Point second, Point midpoint) {
        return lineWithCoefficients(
                second.x() - first.x(),
                second.y() - first.y(),
                -((second.x() - first.x()) * midpoint.x()
                        + (second.y() - first.y()) * midpoint.y()));
    }

    private LineEquation lineThrough(Point start, Point end) {
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        return lineWithCoefficients(dy, -dx, dx * start.y() - dy * start.x());
    }

    private LineEquation lineWithCoefficients(double coefficientX, double coefficientY, double constant) {
        String implicit = coefficientX + "x + " + coefficientY + "y + " + constant + " = 0";
        double directionX = -coefficientY;
        double directionY = coefficientX;
        double norm = Math.hypot(directionX, directionY);
        if (norm == 0.0) {
            throw new IllegalArgumentException("A triangle line could not be calculated.");
        }
        directionX /= norm;
        directionY /= norm;

        double pointX;
        double pointY;
        if (Math.abs(coefficientX) >= Math.abs(coefficientY)) {
            pointX = -constant / coefficientX;
            pointY = 0.0;
        } else {
            pointX = 0.0;
            pointY = -constant / coefficientY;
        }
        String parametric = "(x, y) = (" + pointX + ", " + pointY + ") + t * ("
                + directionX + ", " + directionY + ")";
        String symmetric = symmetricEquation(pointX, pointY, directionX, directionY);
        String slopeIntercept = coefficientY == 0.0
                ? "x = " + (-constant / coefficientX)
                : "y = " + (-coefficientX / coefficientY) + "x + " + (-constant / coefficientY);
        Double xIntercept = coefficientX == 0.0 ? null : -constant / coefficientX;
        Double yIntercept = coefficientY == 0.0 ? null : -constant / coefficientY;
        String vector = "(x, y) = (" + pointX + ", " + pointY + ") + t * ("
                + (-coefficientY) + ", " + coefficientX + ")";
        String normal = (coefficientX / norm) + "x + " + (coefficientY / norm) + "y + "
                + (constant / norm) + " = 0";
        String interceptForm = xIntercept == null || yIntercept == null
                || xIntercept == 0.0 || yIntercept == 0.0
                ? "Undefined: intercept form requires two nonzero axis intercepts."
                : "x / " + xIntercept + " + y / " + yIntercept + " = 1";
        String polar = "r * (" + formatZero(coefficientX) + " * cos(theta) + "
                + formatZero(coefficientY) + " * sin(theta)) + " + formatZero(constant) + " = 0";
        return new LineEquation(
                vector,
                implicit,
                parametric,
                symmetric,
                slopeIntercept,
                normal,
                polar,
                interceptForm,
                xIntercept,
                yIntercept,
                coefficientX,
                coefficientY,
                constant);
    }

    private String symmetricEquation(double pointX, double pointY, double directionX, double directionY) {
        String xTerm = directionX == 0.0
                ? "x = " + pointX
                : "(x - " + pointX + ") / " + directionX;
        String yTerm = directionY == 0.0
                ? "y = " + pointY
                : "(y - " + pointY + ") / " + directionY;
        if (directionX == 0.0 || directionY == 0.0) {
            return xTerm + "; " + yTerm;
        }
        return xTerm + " = " + yTerm;
    }

    private double formatZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    private AngleMeasures calculateAngles(double a, double b, double c, List<CalculationStep> steps) {
        double angleA = angleOppositeSide(a, b, c);
        double angleB = angleOppositeSide(b, a, c);
        double angleC = angleOppositeSide(c, a, b);
        addStep(steps, "Angle A", "A = acos((b^2 + c^2 - a^2) / (2bc))",
                "acos((" + b + "^2 + " + c + "^2 - " + a + "^2) / (2 * " + b + " * " + c + "))", angleA);
        addStep(steps, "Angle B", "B = acos((a^2 + c^2 - b^2) / (2ac))",
                "acos((" + a + "^2 + " + c + "^2 - " + b + "^2) / (2 * " + a + " * " + c + "))", angleB);
        addStep(steps, "Angle C", "C = acos((a^2 + b^2 - c^2) / (2ab))",
                "acos((" + a + "^2 + " + b + "^2 - " + c + "^2) / (2 * " + a + " * " + b + "))", angleC);
        return new AngleMeasures(angleA, angleB, angleC);
    }

    private double angleOppositeSide(double opposite, double adjacentOne, double adjacentTwo) {
        double cosine = (adjacentOne * adjacentOne + adjacentTwo * adjacentTwo - opposite * opposite)
                / (2.0 * adjacentOne * adjacentTwo);
        cosine = Math.max(-1.0, Math.min(1.0, cosine));
        return Math.toDegrees(Math.acos(cosine));
    }

    private void addStep(
            List<CalculationStep> steps,
            String name,
            String formula,
            String substitution,
            double result) {
        ensureFinite(result, "The coordinates produce a result outside the supported numeric range.");
        steps.add(new CalculationStep(name, formula, substitution, result));
    }

    private void ensureFinite(double value, String message) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
