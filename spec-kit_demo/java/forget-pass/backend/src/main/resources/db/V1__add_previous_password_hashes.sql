-- Flyway migration: add previous_password_hashes column to patient_credentials
-- This migration adds a TEXT column to store JSON-serialized previous password hashes.
-- Compatible with SQLite and other databases that accept ALTER TABLE ADD COLUMN.

BEGIN TRANSACTION;

-- Add column for previous password hashes (JSON stored as TEXT for SQLite)
ALTER TABLE patient_credentials
    ADD COLUMN previous_password_hashes TEXT;

COMMIT;
