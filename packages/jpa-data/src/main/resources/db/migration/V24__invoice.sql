CREATE TABLE IF NOT EXISTS t_invoice
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  due_to timestamp(6) without time zone,
  is_canceled boolean NOT NULL,
  billing_id uuid NOT NULL,
  paid_at timestamp(6) without time zone,
  price double precision NOT NULL,
  CONSTRAINT t_invoice_pkey PRIMARY KEY (id),
  CONSTRAINT fk_invoice__to__order FOREIGN KEY (billing_id)
    REFERENCES t_billing (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT t_invoice_price_check CHECK (price >= 0::double precision)
);

ALTER TABLE t_billing RENAME TO t_order;
