CREATE TABLE cryptocurrency
(
    id      BIGSERIAL PRIMARY KEY,
    symbol  VARCHAR(10) UNIQUE,
    title   VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE price_snapshot
(
    id          BIGSERIAL PRIMARY KEY,
    crypto_id   BIGINT REFERENCES cryptocurrency(id) ON DELETE RESTRICT,
    price       NUMERIC(36, 18),
    source      VARCHAR(30),
    fetched_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX price_snapshot_crypto_id_idx ON price_snapshot(crypto_id);
CREATE INDEX cryptocurrency_is_active_idx ON cryptocurrency(is_active) WHERE is_active = TRUE;