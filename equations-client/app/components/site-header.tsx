"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "./auth-provider";

export function SiteHeader() {
  const { session, loading, error: authError, signOut } = useAuth();
  const router = useRouter();
  const [error, setError] = useState("");
  const [signingOut, setSigningOut] = useState(false);

  async function handleSignOut() {
    setError("");
    setSigningOut(true);
    try {
      await signOut();
      router.push("/");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not sign out.");
    } finally {
      setSigningOut(false);
    }
  }

  return (
    <>
      <header className="topbar">
        <Link className="brand" href="/" aria-label="Trigly home">
          <span className="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 32 32" fill="none">
              <path
                d="M16 4 29 27H3L16 4Z"
                stroke="currentColor"
                strokeWidth="2.5"
              />
              <path d="m12 20 4-7 4 7" stroke="currentColor" strokeWidth="2" />
            </svg>
          </span>
          <span>
            Trigly<span className="brand-period">.</span>
          </span>
        </Link>
        <nav className="topbar-actions" aria-label="Main navigation">
          <Link className="topbar-link" href="/">
            Calculator
          </Link>
          {session ? (
            <>
              <Link className="topbar-link" href="/history">
                History
              </Link>
              <span className="topbar-user">{session.user.email}</span>
              <button
                className="topbar-button"
                type="button"
                onClick={handleSignOut}
                disabled={signingOut || loading}
              >
                {signingOut ? "Signing out…" : "Sign out"}
              </button>
            </>
          ) : (
            <Link className="topbar-link topbar-signin" href="/login">
              Sign in
            </Link>
          )}
        </nav>
      </header>
      {(error || authError) && (
        <p className="topbar-error" role="alert">
          {error || authError}
        </p>
      )}
    </>
  );
}
