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

No database or history persistence is configured in this phase. Persistence and authenticated per-user history belong to Phase 2.
