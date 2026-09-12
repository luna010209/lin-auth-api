-- Lin Auth API — MySQL schema
-- Target: MySQL 8.x, utf8mb4
-- Matches: src/main/java/io/lin/auth/feature/**/entity/**
-- Date: 2026-09-12
--
-- Usage (fresh database):
--   mysql -u user -p lin_auth < docs/lin-auth-schema.sql
--
-- Notes:
--   - JPA auditing fills created_by / created_at / updated_by / updated_at at runtime.
--   - Foreign keys are included for documentation; omit if your deployment does not use DB-level FKs.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------------
-- auth — user accounts
-- Entity: io.lin.auth.feature.account.entity.Auth
-- Extends: UserAuditable (created_by, created_at, updated_by, updated_at)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS auth (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    username            VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone               VARCHAR(25)  NULL,
    display_name        VARCHAR(150) NULL,
    avatar              VARCHAR(255) NULL,
    firebase_uid        VARCHAR(255) NULL,
    last_login          DATETIME(6)  NULL,
    created_by          BIGINT       NULL,
    created_at          DATETIME(6)  NOT NULL,
    updated_by          BIGINT       NULL,
    updated_at          DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_username (username),
    UNIQUE KEY uk_auth_email (email),
    UNIQUE KEY uk_auth_firebase_uid (firebase_uid),
    KEY idx_auth_created_at (created_at),
    KEY idx_auth_firebase_uid (firebase_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- auth_roles — user roles (ElementCollection)
-- Values: ROLE_TEACHER, ROLE_ADMIN (see Role enum)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS auth_roles (
    auth_id BIGINT       NOT NULL,
    roles   VARCHAR(255) NOT NULL,
    PRIMARY KEY (auth_id, roles),
    KEY idx_auth_roles_auth_id (auth_id),
    CONSTRAINT fk_auth_roles_auth
        FOREIGN KEY (auth_id) REFERENCES auth (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- email_verification — email verification codes (sign-up / email change)
-- Entity: io.lin.auth.feature.emailverification.entity.EmailVerification
-- created_at only (no UserAuditable / updated_at)
-- Code expires in 20 minutes (application logic)
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS email_verification (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(100) NOT NULL,
    verified_code VARCHAR(100) NOT NULL,
    expires_at    DATETIME(6)  NOT NULL,
    is_verified   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verification_email (email),
    KEY idx_email_verification_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Optional seed (development only — remove or change password before production)
-- ---------------------------------------------------------------------------
-- INSERT INTO auth (username, password, email, display_name, created_at)
-- VALUES ('admin', '{BCRYPT_HASH}', 'admin@example.com', 'Admin', UTC_TIMESTAMP(6));
-- INSERT INTO auth_roles (auth_id, roles) VALUES (1, 'ROLE_ADMIN');

-- ---------------------------------------------------------------------------
-- Migration from legacy table names (run once if upgrading existing data)
-- ---------------------------------------------------------------------------
-- RENAME TABLE langa_auth TO auth;
-- RENAME TABLE langa_auth_roles TO auth_roles;
-- RENAME TABLE langa_email_verification TO email_verification;
--
-- Or from lin_* naming:
-- RENAME TABLE lin_auth TO auth;
-- RENAME TABLE lin_auth_roles TO auth_roles;
-- RENAME TABLE lin_email_verification TO email_verification;
