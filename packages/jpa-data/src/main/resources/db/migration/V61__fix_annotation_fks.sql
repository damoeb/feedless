ALTER TABLE IF EXISTS t_annotation_text DROP CONSTRAINT IF EXISTS fk2wrv2qn79kvxyjtbe7mach2vg;

ALTER TABLE t_annotation_text
  ADD CONSTRAINT fk_annotation_entity FOREIGN KEY (id)
  REFERENCES t_annotation(id)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;

ALTER TABLE IF EXISTS t_annotation_vote DROP CONSTRAINT IF EXISTS fk8v9srbb30wqn2nbn8t3yy5i1e;

ALTER TABLE t_annotation_vote
  ADD CONSTRAINT fk_annotation_entity FOREIGN KEY (id)
  REFERENCES t_annotation(id)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;
