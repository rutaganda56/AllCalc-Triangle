"use client";

import Link from "next/link";
import { useCallback, useEffect, useRef, useState } from "react";
import { SiteHeader } from "../components/site-header";
import { useAuth } from "../components/auth-provider";
import { formatCalculationText, formatNumber, formatPoint } from "../lib/format";

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
  circumcircle: { center: Point; radius: number; area: number; equation: string };
  lines: { bySide: Record<string, SideLineEquations> };
  steps: { name: string; formula: string; substitution: string; result: number }[];
};
type HistoryEntry = {
  id: string;
  title: string;
  calculation: CalculationResponse;
  createdAt: string;
};
type ApiError = { message?: string };

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export default function HistoryPage() {
  const { session, loading: authLoading } = useAuth();
  const [entries, setEntries] = useState<HistoryEntry[]>([]);
  const [loadedUserId, setLoadedUserId] = useState("");
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState("");
  const historyRequestId = useRef(0);

  const loadHistory = useCallback(async () => {
    if (!session) return;
    const requestId = ++historyRequestId.current;

    const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
    return fetch(`${apiUrl.replace(/\/$/, "")}/api/v1/history`, {
      headers: { Authorization: `Bearer ${session.access_token}` },
      cache: "no-store",
    }).then(async (response) => {
      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Your sign-in has expired. Please sign in again.");
        }
        const apiError = (await response.json()) as ApiError;
        throw new Error(apiError.message || `Could not load history (${response.status}).`);
      }
      return (await response.json()) as HistoryEntry[];
    }).then((history) => {
      if (requestId !== historyRequestId.current) return;
      setError("");
      setEntries(history);
      setLoadedUserId(session.user.id);
    }).catch((cause: unknown) => {
      if (requestId !== historyRequestId.current) return;
      setEntries([]);
      setLoadedUserId(session.user.id);
      setError(
        cause instanceof TypeError
          ? "Unable to reach the history API. Check that it is running and configured."
          : cause instanceof Error
            ? cause.message
            : "Could not load saved calculations.",
      );
    });
  }, [session]);

  useEffect(() => {
    if (!session) {
      historyRequestId.current += 1;
      return;
    }

    void loadHistory();
    return () => {
      historyRequestId.current += 1;
    };
  }, [loadHistory, session]);

  async function deleteEntry(id: string) {
    if (!session) return;
    const entry = entries.find((candidate) => candidate.id === id);
    if (entry && !window.confirm(`Delete "${entry.title}" from your history?`)) return;
    setDeletingId(id);
    setError("");
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8081";
      const response = await fetch(`${apiUrl.replace(/\/$/, "")}/api/v1/history/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${session.access_token}` },
      });
      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Your sign-in has expired. Please sign in again.");
        }
        const apiError = (await response.json()) as ApiError;
        throw new Error(apiError.message || `Could not delete calculation (${response.status}).`);
      }
      setEntries((current) => current.filter((entry) => entry.id !== id));
    } catch (cause) {
      setError(cause instanceof TypeError
        ? "Unable to reach the history API."
        : cause instanceof Error
          ? cause.message
          : "Could not delete this calculation.");
    } finally {
      setDeletingId("");
    }
  }

  return (
    <main className="app-shell">
      <SiteHeader />
      <section className="history-page page-content">
        <div className="history-heading">
          <div>
            <span className="section-kicker">YOUR WORKSPACE</span>
            <h1>Saved calculations</h1>
            <p>Calculations you chose to keep, ready when you need them.</p>
          </div>
          <Link className="history-new-button" href="/">New calculation <span aria-hidden="true">↗</span></Link>
        </div>

        {authLoading || (session && loadedUserId !== session.user.id) ? (
          <div className="history-state" role="status">Loading your saved calculations…</div>
        ) : !session ? (
          <div className="history-state history-empty">
            <span className="history-empty-icon" aria-hidden="true">⌂</span>
            <h2>Sign in to view your history</h2>
            <p>Your saved calculations are private to your account.</p>
            <Link className="auth-submit history-signin" href="/login">Sign in</Link>
          </div>
        ) : error && entries.length === 0 ? (
          <div className="history-state" role="alert">
            <p>{error}</p>
            <button className="history-retry-button" type="button" onClick={() => void loadHistory()}>
              Try again
            </button>
          </div>
        ) : entries.length === 0 ? (
          <div className="history-state history-empty">
            <span className="history-empty-icon" aria-hidden="true">△</span>
            <h2>No saved calculations yet</h2>
            <p>Calculate a triangle, then choose “Save to history” to keep it here.</p>
            <Link className="auth-submit history-signin" href="/">Go to calculator</Link>
          </div>
        ) : (
          <>
            {error && <p className="auth-error history-alert" role="alert">{error}</p>}
            <div className="history-list">
              {entries.map((entry) => (
                <article className="history-card" key={entry.id}>
                  <div className="history-card-heading">
                    <div>
                      <h2>{entry.title}</h2>
                      <time dateTime={entry.createdAt}>{formatDate(entry.createdAt)}</time>
                    </div>
                    <button
                      className="history-delete-button"
                      type="button"
                      onClick={() => void deleteEntry(entry.id)}
                      disabled={deletingId === entry.id}
                      aria-label={`Delete ${entry.title}`}
                    >
                      {deletingId === entry.id ? "Deleting…" : "Delete"}
                    </button>
                  </div>
                  <div className="history-coordinates">
                    {(["a", "b", "c"] as const).map((vertex) => {
                      const point = entry.calculation.vertices[vertex];
                      return (
                        <span key={vertex}>
                          <strong>{vertex.toUpperCase()}</strong> ({formatNumber(point.x)}, {formatNumber(point.y)})
                        </span>
                      );
                    })}
                  </div>
                  <div className="history-metrics">
                    <div><span>Area</span><strong>{formatNumber(entry.calculation.area)} <small>sq units</small></strong></div>
                    <div><span>Perimeter</span><strong>{formatNumber(entry.calculation.perimeter)} <small>units</small></strong></div>
                  </div>
                  <details className="advanced-details history-calculation-details">
                    <summary>
                      <span>Equations &amp; working</span>
                      <span className="details-count">{entry.calculation.steps.length} calculation steps</span>
                      <span className="details-chevron" aria-hidden="true">⌄</span>
                    </summary>
                    <div className="advanced-content">
                      <div className="history-result-details">
                        <section className="equations-block">
                          <h3>Sides &amp; angles</h3>
                          {(["a", "b", "c"] as const).map((side) => (
                            <div className="detail-row" key={side}>
                              <span>Side {side} · angle {side.toUpperCase()}</span>
                              <strong>
                                {formatNumber(entry.calculation.sides[side])} units ·{" "}
                                {formatNumber(entry.calculation.angles[side])}°
                              </strong>
                            </div>
                          ))}
                        </section>
                        <section className="equations-block">
                          <h3>Triangle centers</h3>
                          {([
                            ["Centroid · G", entry.calculation.centers.centroid],
                            ["Incenter · I", entry.calculation.centers.incenter],
                            ["Orthocenter · H", entry.calculation.centers.orthocenter],
                            ["Circumcenter · O", entry.calculation.centers.circumcenter],
                          ] as const).map(([label, point]) => (
                            <div className="detail-row" key={label}>
                              <span>{label}</span>
                              <strong>{formatPoint(point)}</strong>
                            </div>
                          ))}
                        </section>
                        <section className="equations-block">
                          <h3>Circle equations</h3>
                          <div className="detail-row">
                            <span>Incircle</span>
                            <strong>{formatCalculationText(entry.calculation.incircle.equation)}</strong>
                          </div>
                          <div className="detail-row">
                            <span>Circumcircle</span>
                            <strong>{formatCalculationText(entry.calculation.circumcircle.equation)}</strong>
                          </div>
                          <div className="detail-row">
                            <span>Incircle radius · area</span>
                            <strong>
                              {formatNumber(entry.calculation.incircle.radius)} units ·{" "}
                              {formatNumber(entry.calculation.incircle.area)} sq units
                            </strong>
                          </div>
                          <div className="detail-row">
                            <span>Circumcircle radius · area</span>
                            <strong>
                              {formatNumber(entry.calculation.circumcircle.radius)} units ·{" "}
                              {formatNumber(entry.calculation.circumcircle.area)} sq units
                            </strong>
                          </div>
                        </section>
                      </div>
                      <section className="equations-block">
                        <h3>Side, median, altitude &amp; bisector equations</h3>
                        {Object.entries(entry.calculation.lines.bySide).map(([side, equations]) => (
                          <div className="line-equation-group" key={side}>
                            <h4>{side}</h4>
                            {([
                              ["Side", equations.side],
                              ["Median", equations.median],
                              ["Altitude", equations.altitude],
                              ["Perpendicular bisector", equations.perpendicularBisector],
                            ] as const).map(([lineType, line]) => (
                              <div className="equation-line" key={lineType}>
                                <h5>{lineType}</h5>
                                {([
                                  ["Vector", "vector"],
                                  ["Parametric", "parametric"],
                                  ["Symmetric", "symmetric"],
                                  ["Implicit", "implicit"],
                                  ["Explicit (slope-intercept)", "slopeIntercept"],
                                  ["Normal", "normal"],
                                  ["Polar", "polar"],
                                  ["Intercept", "interceptForm"],
                                ] as const).map(([label, key]) => (
                                  <div className="detail-row" key={key}>
                                    <span>{label}</span>
                                    <strong>{formatCalculationText(line[key])}</strong>
                                  </div>
                                ))}
                              </div>
                            ))}
                          </div>
                        ))}
                      </section>
                      <section className="equations-block">
                        <h3>Calculation steps</h3>
                        <ol className="steps-list">
                          {entry.calculation.steps.map((step, index) => (
                            <li key={`${step.name}-${index}`}>
                              <span className="step-number">{String(index + 1).padStart(2, "0")}</span>
                              <div>
                                <strong>{step.name}</strong>
                                <p>{formatCalculationText(step.formula)}</p>
                                <code>
                                  {formatCalculationText(step.substitution)} = {formatNumber(step.result)}
                                </code>
                              </div>
                            </li>
                          ))}
                        </ol>
                      </section>
                    </div>
                  </details>
                </article>
              ))}
            </div>
            <p className="history-limit-note">Showing your 100 most recent saved calculations.</p>
          </>
        )}
      </section>
    </main>
  );
}
