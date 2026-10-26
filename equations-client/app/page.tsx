"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { SiteHeader } from "./components/site-header";
import { useAuth } from "./components/auth-provider";
import { formatCalculationText, formatNumber, formatPoint } from "./lib/format";

type Point = { x: number; y: number };
type LineEquation = {
  vector: string;
  implicit: string;
  parametric: string;
  symmetric: string;
  slopeIntercept: string;
  normal: string;
  polar: string;
  interceptForm: string;
};
type SideLineEquations = {
  side: LineEquation;
  median: LineEquation;
  altitude: LineEquation;
  perpendicularBisector: LineEquation;
};
type CalculationResponse = {
  vertices: { a: Point; b: Point; c: Point };
  sides: { a: number; b: number; c: number };
  angles: { a: number; b: number; c: number };
  area: number;
  perimeter: number;
  centers: {
    centroid: Point;
    incenter: Point;
    orthocenter: Point;
    circumcenter: Point;
  };
  incircle: { center: Point; radius: number; area: number; equation: string };
  circumcircle: {
    center: Point;
    radius: number;
    area: number;
    equation: string;
  };
  lines: { bySide: Record<string, SideLineEquations> };
  steps: { name: string; formula: string; substitution: string; result: number }[];
};
type CoordinateKey = "x1" | "y1" | "x2" | "y2" | "x3" | "y3";
type Coordinates = Record<CoordinateKey, string>;
type ApiError = { code?: string; message?: string; details?: Record<string, string> };

const initialCoordinates: Coordinates = {
  x1: "0",
  y1: "0",
  x2: "6",
  y2: "0",
  x3: "2",
  y3: "5",
};

const coordinateGroups: { vertex: string; x: CoordinateKey; y: CoordinateKey }[] = [
  { vertex: "A", x: "x1", y: "y1" },
  { vertex: "B", x: "x2", y: "y2" },
  { vertex: "C", x: "x3", y: "y3" },
];

const equationForms: { label: string; key: keyof LineEquation }[] = [
  { label: "Vector", key: "vector" },
  { label: "Parametric", key: "parametric" },
  { label: "Symmetric", key: "symmetric" },
  { label: "Implicit", key: "implicit" },
  { label: "Explicit (slope-intercept)", key: "slopeIntercept" },
  { label: "Normal", key: "normal" },
  { label: "Polar", key: "polar" },
  { label: "Intercept", key: "interceptForm" },
];

function TrianglePlot({ data }: { data: CalculationResponse }) {
  const width = 440;
  const height = 280;
  const padding = 36;
  const vertices = [
    { ...data.vertices.a, label: "A" },
    { ...data.vertices.b, label: "B" },
    { ...data.vertices.c, label: "C" },
  ];
  const minX = Math.min(...vertices.map(({ x }) => x));
  const maxX = Math.max(...vertices.map(({ x }) => x));
  const minY = Math.min(...vertices.map(({ y }) => y));
  const maxY = Math.max(...vertices.map(({ y }) => y));
  const rangeX = maxX - minX || 1;
  const rangeY = maxY - minY || 1;
  const scale = Math.min((width - padding * 2) / rangeX, (height - padding * 2) / rangeY);
  const offsetX = (width - rangeX * scale) / 2;
  const offsetY = (height - rangeY * scale) / 2;
  const plotPoints = vertices.map((point) => ({
    ...point,
    plotX: offsetX + (point.x - minX) * scale,
    plotY: height - offsetY - (point.y - minY) * scale,
  }));
  const polygon = plotPoints.map(({ plotX, plotY }) => `${plotX},${plotY}`).join(" ");
  const centroidX = offsetX + (data.centers.centroid.x - minX) * scale;
  const centroidY = height - offsetY - (data.centers.centroid.y - minY) * scale;

  return (
    <svg
      className="triangle-plot"
      viewBox={`0 0 ${width} ${height}`}
      role="img"
      aria-label={`Triangle with vertices ${vertices
        .map(({ label, x, y }) => `${label} at ${formatPoint({ x, y })}`)
        .join(", ")}`}
    >
      <defs>
        <pattern id="plot-grid" width="28" height="28" patternUnits="userSpaceOnUse">
          <path d="M 28 0 L 0 0 0 28" className="plot-grid-line" fill="none" />
        </pattern>
      </defs>
      <rect width={width} height={height} rx="12" className="plot-background" />
      <rect width={width} height={height} rx="12" fill="url(#plot-grid)" />
      <polygon points={polygon} className="triangle-shape" />
      {plotPoints.map(({ label, plotX, plotY }) => (
        <g key={label}>
          <circle cx={plotX} cy={plotY} r="5.5" className="vertex-dot" />
          <text
            x={plotX + (plotX > width - 56 ? -12 : 12)}
            y={plotY - 12}
            textAnchor={plotX > width - 56 ? "end" : "start"}
            className="vertex-label"
          >
            {label}
          </text>
        </g>
      ))}
      <circle cx={centroidX} cy={centroidY} r="4" className="centroid-dot" />
      <text x={centroidX + 9} y={centroidY + 15} className="center-label">
        G
      </text>
    </svg>
  );
}

function MetricCard({
  label,
  value,
  unit,
  icon,
}: {
  label: string;
  value: string;
  unit: string;
  icon: string;
}) {
  return (
    <article className="metric-card">
      <span className="metric-icon" aria-hidden="true">
        {icon}
      </span>
      <div>
        <p className="metric-label">{label}</p>
        <p className="metric-value">
          {value} <span>{unit}</span>
        </p>
      </div>
    </article>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="detail-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

export default function Home() {
  const { session } = useAuth();
  const [coordinates, setCoordinates] = useState<Coordinates>(initialCoordinates);
  const [result, setResult] = useState<CalculationResponse | null>(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [savedMessage, setSavedMessage] = useState("");
  const [saveError, setSaveError] = useState("");

  function updateCoordinate(key: CoordinateKey, value: string) {
    setCoordinates((current) => ({ ...current, [key]: value }));
    setResult(null);
    setSavedMessage("");
    setSaveError("");
  }

  async function calculate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSavedMessage("");
    setSaveError("");

    const values = Object.entries(coordinates) as [CoordinateKey, string][];
    const invalidCoordinate = values.find(([, value]) => {
      const number = Number(value);
      return (
        value.trim() === "" ||
        !Number.isInteger(number) ||
        number < -2147483648 ||
        number > 2147483647
      );
    });

    if (invalidCoordinate) {
      setError(`${invalidCoordinate[0]} must be a whole number within the supported range.`);
      return;
    }

    const payload = {
      x1: Number(coordinates.x1),
      y1: Number(coordinates.y1),
      x2: Number(coordinates.x2),
      y2: Number(coordinates.y2),
      x3: Number(coordinates.x3),
      y3: Number(coordinates.y3),
    };

    setIsLoading(true);
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/v1/calculations`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const apiError = (await response.json()) as ApiError;
        const detail = Object.values(apiError.details ?? {})[0];
        throw new Error(detail || apiError.message || `Calculation failed (${response.status}).`);
      }

      const calculation = (await response.json()) as CalculationResponse;
      setResult(calculation);
    } catch (cause) {
      setError(
        cause instanceof TypeError
          ? "Unable to reach the calculation API. Check that it is running and NEXT_PUBLIC_API_URL is correct."
          : cause instanceof Error
            ? cause.message
            : "Something went wrong while calculating this triangle.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  async function saveToHistory() {
    if (!session) return;
    setIsSaving(true);
    setSaveError("");
    setSavedMessage("");
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/v1/history`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${session.access_token}`,
        },
        body: JSON.stringify({
          x1: Number(coordinates.x1),
          y1: Number(coordinates.y1),
          x2: Number(coordinates.x2),
          y2: Number(coordinates.y2),
          x3: Number(coordinates.x3),
          y3: Number(coordinates.y3),
        }),
      });
      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Your sign-in has expired. Please sign in again before saving.");
        }
        const apiError = (await response.json()) as ApiError;
        throw new Error(apiError.message || `Could not save calculation (${response.status}).`);
      }
      setSavedMessage("Saved to your history.");
    } catch (cause) {
      setSaveError(
        cause instanceof TypeError
          ? "Unable to reach the history API. Check that it is running and configured."
          : cause instanceof Error
            ? cause.message
            : "Could not save this calculation.",
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <main className="app-shell">
      <SiteHeader />

      <div className="page-content">
        <section className="intro">
          <div className="eyebrow"><span /> PRECISION GEOMETRY, MADE CLEAR</div>
          <h1>Triangle Geometry Solver</h1>
          <p>
            Enter three points to instantly unveil every measurement, geometric property, and equation behind your triangle
          </p>
        </section>

        <section className="workspace" aria-label="Triangle calculator">
          <aside className="input-panel">
            <div className="panel-heading">
              <div>
                <span className="section-kicker">01 / DEFINE</span>
                <h2>Your coordinates</h2>
              </div>
              <span className="coordinate-badge">XY PLANE</span>
            </div>
            <p className="panel-description">
              Set an x and y coordinate for each vertex.
            </p>

            <form onSubmit={calculate}>
              <div className="coordinate-list">
                {coordinateGroups.map(({ vertex, x, y }, index) => (
                  <fieldset className="coordinate-row" key={vertex}>
                    <legend className="sr-only">Vertex {vertex} coordinates</legend>
                    <span className={`vertex-badge vertex-${vertex.toLowerCase()}`}>
                      {vertex}
                    </span>
                    <label className="coordinate-field">
                      <span className="sr-only">Vertex {vertex} x coordinate</span>
                      <span className="field-axis">x</span>
                      <input
                        aria-label={`${vertex} x coordinate`}
                        type="number"
                        inputMode="numeric"
                        step="1"
                        min="-2147483648"
                        max="2147483647"
                        required
                        disabled={isLoading}
                        value={coordinates[x]}
                        onChange={(event) => updateCoordinate(x, event.target.value)}
                      />
                    </label>
                    <label className="coordinate-field">
                      <span className="sr-only">Vertex {vertex} y coordinate</span>
                      <span className="field-axis">y</span>
                      <input
                        aria-label={`${vertex} y coordinate`}
                        type="number"
                        inputMode="numeric"
                        step="1"
                        min="-2147483648"
                        max="2147483647"
                        required
                        disabled={isLoading}
                        value={coordinates[y]}
                        onChange={(event) => updateCoordinate(y, event.target.value)}
                      />
                    </label>
                    <span className="coordinate-index">0{index + 1}</span>
                  </fieldset>
                ))}
              </div>

              {error && (
                <p className="error-message" role="alert">
                  <span aria-hidden="true">!</span>
                  {error}
                </p>
              )}

              <button className="calculate-button" type="submit" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <span className="button-spinner" aria-hidden="true" />
                    Calculating…
                  </>
                ) : (
                  <>
                    Calculate triangle
                    <span aria-hidden="true">↗</span>
                  </>
                )}
              </button>
            </form>

            <div className="input-footnote">
              <span aria-hidden="true">✳</span>
              Points must be distinct and not collinear.
            </div>
          </aside>

          <section className="visual-panel" aria-label="Triangle preview">
            <div className="visual-heading">
              <div>
                <span className="section-kicker">02 / VISUALIZE</span>
                <h2>{result ? "Your triangle" : "A shape in space"}</h2>
              </div>
              {result && <span className="result-badge">CALCULATED</span>}
            </div>

            {result ? (
              <>
                <div className="plot-wrap">
                  <TrianglePlot data={result} />
                </div>
                <div className="vertex-legend">
                  {coordinateGroups.map(({ vertex }, index) => {
                    const point = [result.vertices.a, result.vertices.b, result.vertices.c][index];
                    return (
                      <div className="legend-item" key={vertex}>
                        <span className={`legend-dot legend-${vertex.toLowerCase()}`} />
                        <span className="legend-name">{vertex}</span>
                        <span className="legend-coordinate">{formatPoint(point)}</span>
                      </div>
                    );
                  })}
                  <div className="legend-item">
                    <span className="legend-dot legend-centroid" />
                    <span className="legend-name">G</span>
                    <span className="legend-coordinate">Centroid</span>
                  </div>
                </div>
              </>
            ) : (
              <div className="empty-visual">
                <div className="empty-visual-art" aria-hidden="true">
                  <svg viewBox="0 0 360 210" fill="none">
                    <path d="M32 174H326M58 192V22" stroke="currentColor" strokeWidth="1" />
                    <path d="m321 170 5 4-5 4M54 27l4-5 4 5" stroke="currentColor" />
                    <path
                      d="m94 148 171 0-70-112-101 112Z"
                      fill="currentColor"
                      fillOpacity=".08"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeDasharray="5 5"
                    />
                    <circle cx="94" cy="148" r="5" fill="currentColor" />
                    <circle cx="265" cy="148" r="5" fill="currentColor" />
                    <circle cx="195" cy="36" r="5" fill="currentColor" />
                    <path d="M179 111h32m-16-16v32" stroke="currentColor" strokeWidth="1.5" />
                  </svg>
                  <span className="art-label art-a">A</span>
                  <span className="art-label art-b">B</span>
                  <span className="art-label art-c">C</span>
                </div>
                <h3>Ready when you are</h3>
                <p>Your plotted triangle and its key points will appear here.</p>
                <span className="empty-visual-hint">Start with the sample coordinates</span>
              </div>
            )}
          </section>
        </section>

        {result ? (
          <section className="results-section" aria-label="Triangle results" aria-live="polite">
            <div className="results-heading">
              <div>
                <span className="section-kicker">03 / EXPLORE</span>
                <h2>The details, at a glance.</h2>
              </div>
              <span className="results-caption">All measurements use your coordinate units.</span>
            </div>

            <div className="metrics-grid">
              <MetricCard
                label="Area"
                value={formatNumber(result.area)}
                unit="sq units"
                icon="◩"
              />
              <MetricCard
                label="Perimeter"
                value={formatNumber(result.perimeter)}
                unit="units"
                icon="↗"
              />
              <MetricCard
                label="Longest side"
                value={formatNumber(Math.max(result.sides.a, result.sides.b, result.sides.c))}
                unit="units"
                icon="⟷"
              />
            </div>

            <div className="insights-grid">
              <article className="insight-card">
                <div className="insight-heading">
                  <span className="insight-icon">△</span>
                  <div>
                    <h3>Sides &amp; angles</h3>
                    <p>Opposite each matching vertex</p>
                  </div>
                </div>
                <div className="detail-list">
                  <DetailRow
                    label="Side a · angle A"
                    value={`${formatNumber(result.sides.a)} units · ${formatNumber(result.angles.a)}°`}
                  />
                  <DetailRow
                    label="Side b · angle B"
                    value={`${formatNumber(result.sides.b)} units · ${formatNumber(result.angles.b)}°`}
                  />
                  <DetailRow
                    label="Side c · angle C"
                    value={`${formatNumber(result.sides.c)} units · ${formatNumber(result.angles.c)}°`}
                  />
                </div>
              </article>

              <article className="insight-card">
                <div className="insight-heading">
                  <span className="insight-icon center-icon">⊙</span>
                  <div>
                    <h3>Triangle centers</h3>
                    <p>Four points with special properties</p>
                  </div>
                </div>
                <div className="detail-list">
                  <DetailRow label="Centroid · G" value={formatPoint(result.centers.centroid)} />
                  <DetailRow label="Incenter · I" value={formatPoint(result.centers.incenter)} />
                  <DetailRow label="Orthocenter · H" value={formatPoint(result.centers.orthocenter)} />
                  <DetailRow label="Circumcenter · O" value={formatPoint(result.centers.circumcenter)} />
                </div>
              </article>

              <article className="insight-card">
                <div className="insight-heading">
                  <span className="insight-icon">◉</span>
                  <div>
                    <h3>Inscribed &amp; circumscribed</h3>
                    <p>Circles around and within your triangle</p>
                  </div>
                </div>
                <div className="detail-list">
                  <DetailRow
                    label="Incircle radius"
                    value={`${formatNumber(result.incircle.radius)} units`}
                  />
                  <DetailRow
                    label="Incircle area"
                    value={`${formatNumber(result.incircle.area)} sq units`}
                  />
                  <DetailRow
                    label="Circumcircle radius"
                    value={`${formatNumber(result.circumcircle.radius)} units`}
                  />
                  <DetailRow
                    label="Circumcircle area"
                    value={`${formatNumber(result.circumcircle.area)} sq units`}
                  />
                </div>
              </article>
            </div>

            {session ? (
              <details className="advanced-details">
                <summary>
                  <span>Equations &amp; working</span>
                  <span className="details-count">{result.steps.length} calculation steps</span>
                  <span className="details-chevron" aria-hidden="true">⌄</span>
                </summary>
                <div className="advanced-content">
                  <div className="equations-block">
                    <h3>Circle equations</h3>
                    <DetailRow label="Incircle" value={formatCalculationText(result.incircle.equation)} />
                    <DetailRow label="Circumcircle" value={formatCalculationText(result.circumcircle.equation)} />
                  </div>
                  <div className="equations-block">
                    <h3>Side, median, altitude &amp; bisector lines</h3>
                    {Object.entries(result.lines.bySide).map(([side, equations]) => (
                      <div className="line-equation-group" key={side}>
                        <h4>{side}</h4>
                        {(
                          [
                            ["Side", equations.side],
                            ["Median", equations.median],
                            ["Altitude", equations.altitude],
                            ["Perpendicular bisector", equations.perpendicularBisector],
                          ] as const
                        ).map(([lineType, line]) => (
                          <div className="equation-line" key={lineType}>
                            <h5>{lineType}</h5>
                            {equationForms.map(({ label, key }) => (
                              <DetailRow key={key} label={label} value={formatCalculationText(line[key])} />
                            ))}
                          </div>
                        ))}
                      </div>
                    ))}
                  </div>
                </div>
              </details>
            ) : (
              <div className="advanced-details advanced-locked">
                <p>
                  <Link href="/login">Sign in</Link> to view equations and working in your saved history.
                </p>
              </div>
            )}
            <div className="save-history-actions">
              {session ? (
                <>
                  <button
                    className="history-new-button"
                    type="button"
                    onClick={saveToHistory}
                    disabled={isSaving}
                  >
                    {isSaving ? "Saving…" : "Save to history"}
                  </button>
                  {savedMessage && (
                    <p className="auth-success" role="status">
                      {savedMessage} <Link href="/history">View history</Link>
                    </p>
                  )}
                  {saveError && <p className="auth-error" role="alert">{saveError}</p>}
                </>
              ) : (
                <p>
                  <Link href="/login">Sign in</Link> to save this calculation to your history.
                </p>
              )}
            </div>
          </section>
        ) : (
          <section className="how-it-works" aria-label="How it works">
            <span className="section-kicker">BUILT FOR CLARITY</span>
            <div className="how-steps">
              <div><span>01</span><p>Enter three points on the coordinate plane</p></div>
              <div><span>02</span><p>See your triangle come to life</p></div>
              <div><span>03</span><p>Explore its measurements and properties</p></div>
            </div>
          </section>
        )}
      </div>

      <footer className="footer">
        <span>Trigly<span className="brand-period">.</span></span>
        <span>Thoughtful tools for curious minds.</span>
        <span>Made for the love of geometry <span aria-hidden="true">✳</span></span>
      </footer>
    </main>
  );
}
