# Trigly

A responsive triangle-calculator frontend built with Next.js. Enter three integer
coordinate pairs to visualize a triangle and explore its sides, angles, area,
perimeter, centers, circles, line equations, and calculation steps.
Sign in to save calculations to a private history.

## Run locally

Start the Spring Boot API from `equations-api` (it listens on port `8081` by
default), then start the frontend:

```bash
bun install
bun dev
```

Open [http://localhost:3000](http://localhost:3000). The frontend sends
calculations to `http://localhost:8081/api/v1/calculations` by default. To use a
different API origin, set `NEXT_PUBLIC_API_URL` before starting or building the
frontend.

The API must allow the frontend origin through its CORS configuration.
For Supabase account setup, Google OAuth configuration, and saved-history
database settings, see the repository [README](../README.md#accounts-and-saved-history).
