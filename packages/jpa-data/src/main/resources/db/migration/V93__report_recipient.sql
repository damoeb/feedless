-- One row per recipient address; opt_in_required is set when its owner reports abuse.
CREATE TABLE t_report_recipient
(
  id              uuid                           NOT NULL PRIMARY KEY,
  created_at      timestamp(6) without time zone NOT NULL DEFAULT NOW(),
  email           character varying(255)         NOT NULL,
  opt_in_required boolean                        NOT NULL DEFAULT false,
  CONSTRAINT uq_report_recipient__email UNIQUE (email)
);

-- Serves disabling every report to one address, which matches on the normalized address.
CREATE INDEX report_recipient_email_normalized_idx ON t_report (lower(trim(recipient_email)));
