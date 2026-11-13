# PortfolioPro

Phase 1 provides account registration and JWT authentication. Phase 2 adds an Indian-stock simulator, watchlists, virtual wallet, orders, holdings, and portfolio metrics. Phase 3 provides an API-backed responsive dashboard, market search/detail and price charts, trading, portfolio, and order history. The repository is split into `frontend/` (React, Vite, Tailwind CSS, React Router, Lightweight Charts) and `backend/` (Java 17, Spring Boot, Maven, Spring Data JPA, Spring Security).

## Run locally

Prerequisites: Node.js 20.19+ and npm, plus Java 17+ and Maven. The local profile uses an in-memory H2 database, so the app can be exercised without Neon credentials.

1. In one PowerShell terminal, generate a signing key and start the API with the local profile:

   ```powershell
   $bytes = New-Object byte[] 32
   $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($bytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
   $rng.Dispose()
   $env:COOKIE_SECURE = "false"
   cd backend
   mvn spring-boot:run "-Dspring-boot.run.profiles=local"
   ```

2. In another terminal, start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173`.

The frontend reads `VITE_API_URL` from `frontend/.env` and defaults to `http://localhost:8080`. See `frontend/.env.example` for the local value.

## Neon configuration

Set these environment variables before starting the backend without the `local` profile. `DATABASE_URL` must be a JDBC URL; use the hostname, database name, username, and password from the Neon connection details. Keep Neon credentials and the JWT signing key out of source control.

```powershell
$env:DATABASE_URL = "jdbc:postgresql://<host>/<database>?sslmode=require"
$env:DATABASE_USERNAME = "<neon-username>"
$env:DATABASE_PASSWORD = "<neon-password>"
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:FRONTEND_URL = "http://localhost:5173"
$env:COOKIE_SECURE = "false"
cd backend
mvn spring-boot:run
```

Use `COOKIE_SECURE=false` and `COOKIE_SAME_SITE=Lax` for local HTTP development. For the separate Vercel frontend and Render backend, use `COOKIE_SECURE=true` and `COOKIE_SAME_SITE=None`; cross-site authenticated requests require `SameSite=None`, and `SameSite=None` requires HTTPS. `JWT_SECRET` must be Base64 and decode to at least 32 bytes. Optional `SERVER_PORT`, `JWT_EXPIRATION_MS` (default 3600000), and `DB_POOL_SIZE` can tune the service. In production, use a managed secrets mechanism, HTTPS, and the exact frontend origin without a trailing slash.

## Verify

```powershell
cd backend
mvn test
cd ..\frontend
npm run lint
npm run build
```

Backend integration tests run with H2 and cover account creation, BCrypt-backed login, stock search and history, simulated price updates, wallet and watchlist behavior, idempotent buy/sell execution, P&L calculations, ownership isolation, and trade validation.

## API

- `POST /api/auth/register` — create an account; returns the user profile and sets the JWT cookie.
- `POST /api/auth/login` — authenticate; returns the user profile and sets the JWT cookie.
- `POST /api/auth/logout` — clear the authentication cookie.
- `GET /api/users/me` — protected current-user profile.
- `GET /api/health` — public liveness check.
- `GET /api/stocks?query=tcs` — search seeded stocks; omit `query` to list all.
- `GET /api/stocks/{symbol}` — current simulated quote and company details.
- `GET /api/stocks/{symbol}/history` — up to 100 most recent price points.
- `POST /api/watchlists` — create the signed-in user's watchlist.
- `GET /api/watchlists/me` — retrieve the signed-in user's watchlist.
- `POST /api/watchlists/me/stocks/{symbol}` — add a stock; duplicate adds are idempotent.
- `DELETE /api/watchlists/me/stocks/{symbol}` — remove a stock.
- `GET /api/wallet` — get the virtual INR balance.
- `POST /api/wallet/top-ups` — add simulated funds; body: `{"amount":100000.00}`.
- `POST /api/orders/buy` and `POST /api/orders/sell` — execute a simulated order; require an `Idempotency-Key` header and body `{"symbol":"TCS","quantity":2}`.
- `GET /api/orders` — current user's latest 100 orders.
- `GET /api/portfolio/holdings` — current user's holdings and mark-to-market values.
- `GET /api/portfolio/summary` — wallet, invested/current value, and realized/unrealized P&L.

All Phase 2 endpoints require authentication. Example buy response:

```json
{
   "id": 12,
   "symbol": "TCS",
   "side": "BUY",
   "quantity": 2,
   "executionPrice": 4120.00,
   "totalAmount": 8240.00,
   "currency": "INR",
   "realizedPnl": 0.00,
   "status": "EXECUTED",
   "createdAt": "2026-11-03T10:15:30"
}
```

The JWT is kept in an `HttpOnly`, `SameSite=Lax` cookie and is not exposed to frontend JavaScript. Auth responses omit password hashes. Stock prices are simulated locally; no external market API or real-money transaction is used. Each order is transactional and uses the server's current price, wallet balance, and holdings. Reusing an `Idempotency-Key` with the same payload returns the original order; reusing it for different order details returns `409 Conflict`.