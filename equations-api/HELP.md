# Equations API

## Run locally

From this directory, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8081` by default to avoid the common local Docker/Traefik port `8080`. Override it with the `PORT` environment variable when needed (Render provides `PORT` for deployed services).

## Calculate a triangle from coordinates

`POST /api/v1/calculations` accepts the same six integer coordinate inputs as the Swing application. Side `a`, `b`, and `c` are opposite points 1, 2, and 3, respectively. Angles are returned in degrees. An optional `sides` query parameter selects which side-equation groups to include; valid values are `AC`, `AB`, and `BC`. Omit it to return all groups.

```text
POST /api/v1/calculations?sides=AC&sides=BC
```

```json
{
  "x1": 0,
  "y1": 0,
  "x2": 3,
  "y2": 0,
  "x3": 0,
  "y3": 4
}
```

Successful responses include side lengths, angles, area, perimeter, triangle centers, incircle/circumcircle values, and separately keyed side-equation groups under `lines.bySide.AC`, `lines.bySide.AB`, and `lines.bySide.BC` as selected. Each group includes its side line, median and altitude from the opposite vertex, their lengths, and that side's perpendicular bisector. Line equations include vector, parametric, symmetric, implicit, explicit, normal, polar, and intercept representations where defined. The polar form is derived from `Ax + By + C = 0` using `x = r cos(theta)` and `y = r sin(theta)`. Intercept form is marked undefined when either axis intercept is zero or does not exist. Values remain full precision in the API response. Missing coordinates, extra request fields, repeated points, collinear points, and unsupported side selections return HTTP `400` with an `ApiError` JSON body. No side/angle-input solver modes are supported.

## CORS

The API allows browser requests to `/api/**` from `http://localhost:3000` by default. Set `APP_CORS_ALLOWED_ORIGINS` to a comma-separated list of exact web origins for deployed environments, for example:

```text
APP_CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:3000
```

## Accounts and saved history

`POST /api/v1/calculations` remains available without authentication. Saved history
uses the authenticated Supabase user:

- `POST /api/v1/history` saves a calculation for the token's user.
- `GET /api/v1/history` returns that user's 100 most recent saved calculations.
- `DELETE /api/v1/history/{id}` deletes an entry only when it belongs to that user.

The API validates Supabase JWT signatures against `SUPABASE_JWKS_URI` and checks
the issuer, audience, and UUID subject. Configure `SUPABASE_JWT_ISSUER` and
`SUPABASE_JWKS_URI` for the same project and use an asymmetric ES256 or RS256
signing key. `DATABASE_URL`,
`DATABASE_USERNAME`, and `DATABASE_PASSWORD` configure PostgreSQL; without them,
the API uses a local in-memory H2 database for development. History is therefore
not persistent across restarts unless a persistent PostgreSQL database is used.
