CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    balance NUMERIC(19, 2) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
