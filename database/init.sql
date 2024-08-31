
CREATE DATABASE IF NOT EXISTS issue_db;

USE db;

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    apikey VARCHAR(255) DEFAULT '',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 'admin'ユーザーを追加
INSERT INTO users (id, username, password, email, apikey, is_deleted)
VALUES (
    UUID(),  -- UUIDを自動生成
    'admin', -- ユーザー名
    '$2a$10$2XA/ubM3.J/ARVJJH/8WEOaMMnj9aqoZP8MqJEmeCoxMGnGIzTWo.', -- ハッシュ化されたパスワード password123
    'admin@example.com', -- メールアドレス
    '',  -- APIキー
    FALSE    -- is_deletedフラグ
);

CREATE TABLE tokens (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(255) NOT NULL,
    id_token VARCHAR(255),
    refresh_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_revoked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);