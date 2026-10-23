# AllCalc Triangle

A triangle calculator with a Next.js frontend and a Spring Boot calculation API.
Enter three coordinate points to calculate triangle measurements, centers,
circles, line equations, and working steps.

## Requirements

- Java 17 or later
- Bun 1.2.20 or later
- A Supabase project for persistent accounts and saved history

## Run locally

Start the API in one terminal:

```powershell
Set-Location equations-api
.\mvnw.cmd spring-boot:run
```

Start the frontend in another terminal:

```powershell
Set-Location equations-client
bun install
bun dev
```

Open [http://localhost:3000](http://localhost:3000). The frontend uses the API
at `http://localhost:8081` by default. Set `NEXT_PUBLIC_API_URL` to use a
different API origin, and configure `APP_CORS_ALLOWED_ORIGINS` on the API to
allow the frontend's origin.

## Accounts and saved history

Copy `equations-client/.env.example` to `equations-client/.env.local` and set
the Supabase project URL and publishable (or anon) key. Copy
`equations-api/.env.example` as a reference and set those values in the API
process environment or IDE launch configuration; Spring Boot does not load
`.env` files automatically. Do not commit local environment files. The frontend
key is public by design; database credentials and signing configuration must
remain server-side.

In the Supabase dashboard:

1. Enable email/password and Google under **Authentication → Sign In / Providers**.
2. Use an asymmetric Supabase JWT signing key (ES256 or RS256); the API verifies
   tokens through the project's public JWKS endpoint. Supabase recommends
   asymmetric signing keys for backend token validation.
3. Configure Google OAuth credentials with the Supabase callback URL shown in
   the provider settings. Add the frontend's `/auth/callback` URL to Supabase's
   allowed redirect URLs and set the correct site URL. Add the local and
   deployed callback URLs if you'll use both environments.
4. Set the API database URL, username, and password using the connection details
   from **Project Settings → Database**. The API creates the history table on
   startup. The local default is an in-memory H2 database; use PostgreSQL for
   history that persists across restarts.
5. Set `SUPABASE_JWT_ISSUER` and `SUPABASE_JWKS_URI` for that same Supabase
   project. The API validates signed access tokens and scopes all history reads
   and deletes to the authenticated user's ID.

Users can calculate without signing in. They must sign in before saving a
calculation; saved results are private to the account. The history page lists
the 100 most recent saved calculations and allows users to delete their own
entries.

## Verify

Run API tests from `equations-api`:

```powershell
.\mvnw.cmd test
```

Run frontend checks from `equations-client`:

```powershell
bun run lint
bun run build
```
