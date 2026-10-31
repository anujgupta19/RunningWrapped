# Data model

Postgres. Flyway migrations in `backend/src/main/resources/db/migration`. Tables are added **incrementally per milestone**,
not all at once. This doc is the target shape and gets updated as we learn.

Conventions: `uuid` PKs · `timestamptz` for instants · canonical units (meters, seconds) · `snake_case` · singular table names
· `created_at`/`updated_at` on mutable tables · enums stored as `text` + `CHECK` (easier to evolve than PG enums).

## Changes vs the original spec (and why)

| Change | Reason |
|---|---|
| `app_user` instead of `user` | `user` is a reserved word in Postgres |
| `password_hash`, `display_name`, `preferred_units`, `timezone`, `week_start`, `ai_consent_at` on users | auth, unit display, correct weekly bucketing, recorded consent |
| `activity.timezone`, `activity.name`, `activity.import_id` | "favorite running day" and weekly bounds need the run's local time; trace an activity back to its import |
| `activity_best_effort` table | honest distance PRs from real segments |
| `activity_import` table | import history screen + idempotency/debugging |
| `refresh_token` table | rotating, revocable sessions |
| `push_token` table (Phase 5) | notifications |
| Recap unique key + `status` | idempotent generation, async AI step |

## Tables

### app_user
| column | type | notes |
|---|---|---|
| id | uuid PK | |
| email | text not null | unique on `lower(email)` |
| password_hash | text not null | delegating encoder format (`{bcrypt}...`) |
| display_name | text | |
| preferred_units | text not null default 'MILES' | `MILES` \| `KILOMETERS` |
| timezone | text not null default 'UTC' | IANA, e.g. `America/New_York` |
| week_start | text not null default 'MONDAY' | |
| ai_consent_at | timestamptz null | null = no consent. Set/cleared via PATCH /users/me |
| created_at, updated_at | timestamptz not null | |

### refresh_token
| id uuid PK · user_id → app_user · token_hash text unique · expires_at · revoked_at null · created_at |

### activity_import
| column | type | notes |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK | |
| file_name | text | |
| file_format | text | `FIT` \| `GPX` \| `TCX` |
| status | text | `PROCESSING` \| `COMPLETED` \| `FAILED` |
| activities_created / duplicates_skipped | int | |
| error_message | text null | user-safe message only |
| created_at, completed_at | timestamptz | |
Index: `(user_id, created_at desc)`.

### activity
| column | type | notes |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK not null | |
| import_id | uuid FK null | null for future API-synced activities |
| source | text not null | `FILE_IMPORT` now; `STRAVA`, `APPLE_HEALTH`, `GARMIN` later |
| external_activity_id | text null | id from the file/provider when available |
| activity_type | text not null | `RUN`, `TRAIL_RUN`, `TREADMILL_RUN`, (others stored but excluded from stats) |
| name | text null | |
| start_time | timestamptz not null | |
| timezone | text not null | IANA zone the run happened in (from file or user default) |
| distance_meters | double precision not null | |
| moving_time_seconds | int not null | |
| elapsed_time_seconds | int not null | |
| average_heart_rate / max_heart_rate | smallint null | |
| elevation_gain_meters | double precision null | |
| created_at | timestamptz not null | |

Indexes:
- `(user_id, start_time desc)`: the workhorse for lists and every analytics range query.
- unique `(user_id, source, external_activity_id) WHERE external_activity_id IS NOT NULL`: exact dedupe.
- Fuzzy dedupe uses the first index (range on start_time) and then compares distance in code.

Average pace isn't stored. It's derived (`moving_time / distance`).

### activity_best_effort
| id uuid PK · activity_id → activity (on delete cascade) · user_id (denormalized for PR queries) · distance_key text (`MILE`, `FIVE_K`, `TEN_K`, `HALF_MARATHON`, `MARATHON`) · elapsed_seconds int · start_offset_seconds int |
Index: `(user_id, distance_key, elapsed_seconds)`, so "fastest 5K" is an index scan. Unique `(activity_id, distance_key)`.

Longest run / highest weekly / monthly mileage are aggregates over `activity`. They aren't stored.

### recap
| column | type | notes |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK | |
| recap_type | text | `WEEKLY` \| `MONTHLY` \| `YEARLY` |
| period_start / period_end | date | local dates in the user's timezone |
| status | text | `READY`, `AI_PENDING` |
| statistics_json | jsonb not null | frozen snapshot. Shape versioned by a `schemaVersion` field |
| ai_summary | text null | |
| generated_at | timestamptz | |
Unique `(user_id, recap_type, period_start)`, which makes generation idempotent (`INSERT ... ON CONFLICT DO UPDATE`).

### integration (table created when first needed, not in Phase 1)
| id · user_id · provider · connection_status · last_synced_at · encrypted credentials (envelope-encrypted, never plaintext) |
Unique `(user_id, provider)`.

### push_token (Phase 5)
| id · user_id · expo_push_token unique · platform · created_at · last_seen_at |
