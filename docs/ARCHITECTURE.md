# Architecture

## Big picture

```
┌──────────────────────────┐        HTTPS/JSON         ┌──────────────────────────────────────────┐
│  Mobile (Expo / RN)      │ ───────────────────────▶  │  Spring Boot modular monolith            │
│  Expo Router, TanStack Q │   JWT access token        │                                          │
│  Reanimated, Skia charts │ ◀───────────────────────  │  auth · users · imports · activities     │
│  view-shot → share card  │                           │  analytics · recaps · ai · notifications │
└──────────┬───────────────┘                           └───────┬─────────────────┬────────────────┘
           │ Expo push tokens                                  │ JPA/Flyway      │ only w/ consent
           ▼                                                   ▼                 ▼
     Expo Push Service  ◀── notifications module ──     PostgreSQL         OpenAI API
                                                        (Railway)          (stats in → text out)
```

One deployable backend, one database, one mobile app. Everything else is a library.

## Why a modular monolith (and what "modular" means here)

We get module boundaries from **Java packages + access modifiers**, not from the network:

- Each feature is a package. Classes are package-private by default, so `analytics` *can't* reach into
  `activities`' repository even if it wanted to. The compiler enforces it.
- Cross-feature calls go through a small public service API (e.g. `ActivityQueryService`).
- If we ever wanted stronger checks, Spring Modulith or ArchUnit can verify the boundaries in a test. That's optional and not planned.

Compared to Go: this is like keeping internals unexported (lowercase) and exposing a small package API.

## Modules

| Module | Responsibility | Depends on |
|---|---|---|
| `common` | error handling (ProblemDetail), `PageResponse`, security config, Clock bean, OpenAPI config | (none) |
| `auth` | register/login, JWT issue/verify, refresh-token rotation | users |
| `users` | profile, preferences (units, timezone, week start), AI consent, notification prefs | (none) |
| `imports` | accept uploaded files, detect format, run parser, record import history | activities |
| `activities` | canonical `Activity` model, dedupe, best efforts, list/detail queries | (none) |
| `integrations` | `Integration` records + provider extension point (no providers in MVP) | activities |
| `analytics` | weekly/monthly/yearly aggregates, progress series, personal records | activities, users |
| `recaps` | build + snapshot recap stats (idempotent), schedule periodic generation | analytics, ai, users |
| `ai` | `InsightGenerator` interface; OpenAI implementation; prompt + guardrails; consent check | users |
| `notifications` | push-token registry, sending via Expo push, de-duplication / rate limiting | users |

## Key flows

### File import (MVP data source)
1. `POST /api/v1/activities/import` (multipart, one or many files). The `imports` module creates an `activity_import` row.
2. Detect format by extension + magic bytes, then pick an `ActivityFileParser` (FIT / GPX / TCX).
   Spring injects *all* parser beans as `List<ActivityFileParser>`. This is one of the few places an interface earns its keep.
3. The parser outputs a **canonical `ParsedActivity`** (summary fields + time series of `(t, distance, hr, elevation)` samples).
4. `activities` **dedupes**:
   - exact: unique `(user_id, source, external_activity_id)` (e.g. a FIT file's activity id/serial + time).
   - fuzzy, cross-source: same user, start within ±2 min, distance within ±3% → treated as a duplicate.
5. Compute **best efforts** from the time series (fastest contiguous 1 mi / 5K / 10K / HM / M segment) using a sliding window.
   Store them in `activity_best_effort`. That's how PRs stay honest. We never extrapolate from average pace.
6. Persist the activity and best efforts in one transaction. The import row records created/duplicate/failed counts.

Raw time series are **not** stored in Postgres in the MVP. We derive what we need at import time.
(We could store raw files in object storage later so metrics can be recomputed.)

### Analytics
Computed **on read** with SQL aggregations (`date_trunc` in the user's timezone) over `activity`, which
is indexed by `(user_id, start_time)`. A runner has ~hundreds of activities a year, so this is cheap. We'll only add caching if profiling says so.

Pace vs heart rate ("aerobic efficiency") compares runs **within similar HR bands and similar distance ranges**,
and the API returns the sample size and caveats with every comparison so the UI never overclaims.

### Recaps
- A recap is a **snapshot**: `statistics_json` freezes the numbers at generation time, so a recap doesn't change after the fact.
- **Idempotent:** unique `(user_id, recap_type, period_start)`. Generating again upserts instead of duplicating.
- Periodic generation uses `@Scheduled` jobs (weekly Monday morning user-local, monthly on the 1st, yearly in December).
  Railway runs one instance, so no distributed locking is needed. If we scale out, add ShedLock.
- AI summary is a separate, optional step, only run when `users.ai_consent_at` is set.

### AI insights
- Input is a small JSON of **precomputed** stats (current vs previous period). No raw GPS and no identifiers.
- Output is short text. The system prompt forbids medical claims and causal fitness claims, and requires numbers
  to come only from the input. We validate that the output doesn't contain numbers absent from the input (cheap guardrail).
- The `InsightGenerator` interface has an OpenAI implementation and a deterministic template implementation, used for tests and as a fallback.

## Authentication

- Email + password (MVP). Spring Security, stateless.
- Short-lived **JWT access token** (~15 min) validated by Spring Security's OAuth2 resource-server support,
  plus a **refresh token** (opaque random, stored hashed, rotated on use, revocable).
- Mobile stores tokens in `expo-secure-store`.
- Later: Sign in with Apple (likely wanted for iOS anyway).

## Infrastructure

| Concern | Choice |
|---|---|
| Local DB | `compose.yaml` Postgres 18 (host port 5433). Spring Boot docker-compose support starts it on `spring-boot:run` |
| Tests | Testcontainers Postgres (real DB, no H2) |
| Backend image | Multi-stage Dockerfile (or `spring-boot:build-image`) |
| Hosting | Railway: backend service + managed Postgres |
| Mobile builds | Expo EAS (dev build → TestFlight) |
| CI | GitHub Actions: `./mvnw verify`, mobile typecheck/lint/test |
| Observability | Actuator health/metrics, structured JSON logs, Sentry (backend + mobile) in Phase 5 |
| API docs | springdoc-openapi → `/v3/api-docs`, Swagger UI in dev |

## Decision log

| # | Decision | Why |
|---|---|---|
| 1 | Modular monolith, package-by-feature | Small team/app. Boundaries via package visibility. |
| 2 | UUID primary keys | Non-guessable ids in URLs, safe to generate client- or server-side |
| 3 | Canonical units: meters, seconds, UTC + IANA tz | One source of truth. Weekly/day-of-week stats need the *local* time of the run |
| 4 | Best efforts from time series at import | Honest PRs (spec: never infer a 5K from a longer run's average) |
| 5 | Analytics on read, recaps as snapshots | Simple. Recaps are stable artifacts you can share |
| 6 | No Lombok | Records cover DTOs. Plain Java is better for learning |
| 7 | No Strava in MVP | Developer agreement restrictions on AI/analytics. Revisit later |
| 8 | Self-issued JWT + rotating refresh tokens | Teaches Spring Security. No external IdP dependency |
