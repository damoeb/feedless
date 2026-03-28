CREATE TABLE t_pending_auction_alert (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    segmentation_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pending_auction_alert_email ON t_pending_auction_alert (email);
CREATE INDEX idx_pending_auction_alert_status ON t_pending_auction_alert (status);
