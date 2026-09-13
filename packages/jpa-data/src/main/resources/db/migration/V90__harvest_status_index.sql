-- The queued-harvest executor claims queued harvests oldest first and sweeps stale running ones;
-- both scans filter on status.
CREATE INDEX idx_harvest_status_created_at ON t_harvest (status, created_at);
