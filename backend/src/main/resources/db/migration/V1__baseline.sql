-- Baseline: schema already exists in production (created by Hibernate ddl-auto=update).
-- This migration is skipped via spring.flyway.baseline-version=1 / baseline-on-migrate=true.
-- It documents the full schema as of this point for reference.

CREATE TABLE IF NOT EXISTS households (
    id          UUID PRIMARY KEY,
    invite_code VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS members (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    email         VARCHAR(255),
    password_hash VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tasks (
    id               UUID PRIMARY KEY,
    household_id     UUID,
    title            VARCHAR(255),
    date             DATE,
    assigned_to      UUID,
    status           VARCHAR(255),
    recurrence_type  VARCHAR(255),
    recurrence_weekday VARCHAR(255),
    project_id       UUID
);

CREATE TABLE IF NOT EXISTS projects (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    title         VARCHAR(255),
    goal          VARCHAR(255),
    status        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS fixed_events (
    id               UUID PRIMARY KEY,
    household_id     UUID,
    title            VARCHAR(255),
    date             DATE,
    recurrence_type  VARCHAR(255),
    recurrence_weekday VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS meals (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    title         VARCHAR(255),
    date          DATE,
    status        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS meal_notes (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    date          DATE,
    note          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS recipes (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    title         VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS planned_meals (
    id            UUID PRIMARY KEY,
    household_id  UUID,
    date          DATE,
    title         VARCHAR(255),
    recipe_id     UUID
);
