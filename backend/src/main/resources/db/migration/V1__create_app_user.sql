-- "user" is a reserved word in Postgres, hence app_user.
create table app_user (
    id              uuid        primary key,
    email           text        not null,
    password_hash   text        not null,
    display_name    text,
    preferred_units text        not null default 'MILES'
        check (preferred_units in ('MILES', 'KILOMETERS')),
    timezone        text        not null default 'UTC',  -- IANA zone id, validated in the app
    week_start      text        not null default 'MONDAY'
        check (week_start in ('MONDAY', 'SUNDAY')),
    ai_consent_at   timestamptz,                         -- null = no consent to AI processing
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

-- Emails are unique regardless of case: Foo@x.com and foo@x.com are the same account.
create unique index app_user_email_lower_key on app_user (lower(email));
