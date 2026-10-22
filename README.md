# AllCalc Triangle

A triangle calculator with a Next.js frontend and a Spring Boot calculation API.
Enter three coordinate points to calculate triangle measurements, centers,
circles, line equations, and working steps.

## Requirements

- Java 17 or later
- Bun 1.2.20 or later

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
