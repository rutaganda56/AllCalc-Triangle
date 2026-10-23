"use client";

import Link from "next/link";
import { Suspense, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { supabase } from "../../lib/supabase";

function AuthCallbackContent() {
  const searchParams = useSearchParams();
  const code = searchParams.get("code");
  const [message, setMessage] = useState("Completing sign-in…");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    if (!supabase || !code) return;

    void supabase.auth.exchangeCodeForSession(code).then(({ error: authError }) => {
      if (!active) return;
      if (authError) {
        setError(authError.message);
        setMessage("");
        return;
      }
      window.location.replace("/history");
    }).catch((cause: unknown) => {
      if (!active) return;
      setError(cause instanceof Error ? cause.message : "Could not complete sign-in.");
      setMessage("");
    });

    return () => {
      active = false;
    };
  }, [code]);

  const displayedError = !supabase
    ? "Authentication is not configured."
    : !code
      ? "The sign-in link is missing its authorization code. Please try signing in again."
      : error;

  return (
    <main className="app-shell">
      <section className="auth-page page-content">
        <div className="auth-card callback-card">
          <span className="section-kicker">SECURE SIGN-IN</span>
          <h1>{displayedError ? "Sign-in didn’t finish." : "One moment."}</h1>
          {!displayedError && message && <p className="auth-description" role="status">{message}</p>}
          {displayedError && <p className="auth-error" role="alert">{displayedError}</p>}
          {displayedError && <Link className="auth-back-link" href="/login">Return to sign in</Link>}
        </div>
      </section>
    </main>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={<main className="app-shell"><p className="auth-description">Completing sign-in…</p></main>}>
      <AuthCallbackContent />
    </Suspense>
  );
}
