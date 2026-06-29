CREATE TABLE token_blacklist (
    token VARCHAR(500) PRIMARY KEY,
    blacklisted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_token_blacklist_expires_at ON token_blacklist(expires_at);
