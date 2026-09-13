-- Create repository claim table
CREATE TABLE t_repository_claim
(
  id            uuid                     NOT NULL PRIMARY KEY,
  created_at    timestamp with time zone NOT NULL DEFAULT NOW(),
  repository_id uuid,
  CONSTRAINT fk_repository_claim__to__repository FOREIGN KEY (repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

-- Add index on repository_id for better query performance
CREATE INDEX repository_claim_repository_id_idx ON t_repository_claim (repository_id);

-- Add index on created_at for sorting/filtering
CREATE INDEX repository_claim_created_at_idx ON t_repository_claim (created_at);
