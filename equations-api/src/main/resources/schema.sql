CREATE TABLE IF NOT EXISTS calculation_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    calculation_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS calculation_history_user_created_at_idx
    ON calculation_history (user_id, created_at DESC);
