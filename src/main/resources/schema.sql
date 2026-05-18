/*
 --- General Rules ---
 Use underscore_names instead of CamelCase
 Table names should be plural
 Spell out id fields (item_id instead of id)
 Don't use ambiguous column names
 Name foreign key columns the same as the columns they refer to
 */

CREATE SCHEMA IF NOT EXISTS securecapita;
USE securecapita;

SET NAMES 'utf8mb4';

-- USERS
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(30) DEFAULT NULL,
    title VARCHAR(50) DEFAULT NULL,
    bio VARCHAR(255) DEFAULT NULL,
    enabled BOOLEAN DEFAULT FALSE,
    is_not_locked BOOLEAN DEFAULT TRUE,
    using_mfa BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(255) DEFAULT 'https://cdn-icons-png.flaticon.com/512/149/149071.png',
    CONSTRAINT uq_users_email UNIQUE(email)
);

-- ROLES
DROP TABLE IF EXISTS roles;

CREATE TABLE roles
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- USER_ROLES
DROP TABLE IF EXISTS user_roles;

CREATE TABLE user_roles
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_user_roles_user_id UNIQUE (user_id)
);

-- EVENTS
DROP TABLE IF EXISTS events;

CREATE TABLE events
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type ENUM(
        'LOGIN_ATTEMPT',
        'LOGIN_ATTEMPT_FAILURE',
        'LOGIN_ATTEMPT_SUCCESS',
        'PROFILE_UPDATE',
        'PROFILE_PICTURE_UPDATE',
        'ROLE_UPDATE',
        'ACCOUNT_SETTINGS_UPDATE',
        'PASSWORD_UPDATE',
        'MFA_UPDATE'
    ) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT uq_events_type UNIQUE (type)
);

-- USER_EVENTS
DROP TABLE IF EXISTS user_events;

CREATE TABLE user_events
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    event_id    BIGINT UNSIGNED NOT NULL,
    device      VARCHAR(100) DEFAULT NULL,
    ip_address  VARCHAR(100) DEFAULT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ACCOUNT_VERIFICATIONS
DROP TABLE IF EXISTS account_verifications;

CREATE TABLE account_verifications
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    url VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_account_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_account_verifications_url UNIQUE (url)
);

-- RESET PASSWORD
DROP TABLE IF EXISTS reset_password_verifications;

CREATE TABLE reset_password_verifications
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    url VARCHAR(255) NOT NULL,
    expiration_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_reset_password_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_reset_password_verifications_url UNIQUE (url)
);

-- TWO FACTOR
DROP TABLE IF EXISTS two_factor_verifications;

CREATE TABLE two_factor_verifications
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(10) NOT NULL,
    expiration_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_two_factor_verifications_user_id UNIQUE (user_id),
    CONSTRAINT uq_two_factor_verifications_code UNIQUE (code)
);

