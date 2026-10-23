"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { SiteHeader } from "../components/site-header";
import { useAuth } from "../components/auth-provider";
import { isSupabaseConfigured, supabase } from "../lib/supabase";

type AuthMode = "sign-in" | "sign-up";

export default function LoginPage() {
  const { session, loading } = useAuth();
  const router = useRouter();
  const [mode, setMode] = useState<AuthMode>("sign-in");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [working, setWorking] = useState(false);

  useEffect(() => {
    if (!loading && session) {
      router.replace("/history");
    }
  }, [loading, router, session]);

  async function submitCredentials(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    if (!supabase) {
      setError("Authentication is not configured yet. Add the Supabase settings to the frontend environment.");
      return;
    }

    setWorking(true);
    try {
      if (mode === "sign-up") {
        const { data, error: authError } = await supabase.auth.signUp({ email, password });
        if (authError) throw authError;
        if (!data.session) {
          setMessage("Check your email for a confirmation link before signing in.");
        } else {
          router.replace("/history");
        }
      } else {
        const { error: authError } = await supabase.auth.signInWithPassword({ email, password });
        if (authError) throw authError;
        router.replace("/history");
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Authentication failed.");
    } finally {
      setWorking(false);
    }
  }

  async function signInWithGoogle() {
    setError("");
    if (!supabase) {
      setError("Authentication is not configured yet. Add the Supabase settings to the frontend environment.");
      return;
    }

    setWorking(true);
    try {
      const { error: authError } = await supabase.auth.signInWithOAuth({
        provider: "google",
        options: { redirectTo: `${window.location.origin}/auth/callback` },
      });
      if (authError) {
        setError(authError.message);
        setWorking(false);
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Google sign-in could not be started.");
      setWorking(false);
    }
  }

  return (
    <main className="app-shell">
      <SiteHeader />
      <section className="auth-page page-content">
        <div className="auth-card">
          <span className="section-kicker">YOUR GEOMETRY WORKSPACE</span>
          <h1>{mode === "sign-in" ? "Welcome back." : "Create your account."}</h1>
          <p className="auth-description">
            Sign in to save calculations and return to them later.
          </p>

          {!isSupabaseConfigured && (
            <p className="auth-notice" role="status">
              Supabase isn’t connected yet. Configure the frontend environment variables to enable accounts.
            </p>
          )}

          <button
            className="google-button"
            type="button"
            onClick={signInWithGoogle}
            disabled={working || !isSupabaseConfigured}
          >
            Continue with Google
          </button>

          <div className="auth-divider"><span>or with email</span></div>

          <form className="auth-form" onSubmit={submitCredentials}>
            <label>
              Email
              <input
                autoComplete="email"
                type="email"
                required
                value={email}
                onChange={(event) => setEmail(event.target.value)}
              />
            </label>
            <label>
              Password
              <input
                autoComplete={mode === "sign-in" ? "current-password" : "new-password"}
                type="password"
                required
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            {error && <p className="auth-error" role="alert">{error}</p>}
            {message && <p className="auth-success" role="status">{message}</p>}
            <button
              className="auth-submit"
              type="submit"
              disabled={working || !isSupabaseConfigured}
            >
              {working ? "Please wait…" : mode === "sign-in" ? "Sign in" : "Create account"}
            </button>
          </form>

          <p className="auth-switch">
            {mode === "sign-in" ? "New here?" : "Already have an account?"}{" "}
            <button
              type="button"
              onClick={() => {
                setMode(mode === "sign-in" ? "sign-up" : "sign-in");
                setError("");
                setMessage("");
              }}
            >
              {mode === "sign-in" ? "Create an account" : "Sign in"}
            </button>
          </p>
          <Link className="auth-back-link" href="/">Back to calculator</Link>
        </div>
      </section>
    </main>
  );
}
