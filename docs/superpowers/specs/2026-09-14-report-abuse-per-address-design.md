# Report abuse per address — design

Date: 2026-09-14 · Branch: `feature/weekly-report-e2e` (PR #83) · Status: awaiting review

## Context

Weekly e-mail reports start active without an opt-in step (`c10255608`). Every report mail carries a signed cancel link whose token names one report. Anyone can therefore subscribe an address they do not own. The flow still assumes no abuse. When the owner of an address reports abuse, a flag switches that address alone to opt-in, across all its reports. A configuration setting can instead require opt-in for every address.

## Decisions

| Question | Decision |
|----------|----------|
| How is abuse reported? | A signed "Ich habe das nicht abonniert" link in every report mail |
| Scope | The e-mail address, across all its reports |
| Subscription mode | A setting: `opt-out` (default) activates new subscriptions at once unless the address is flagged; `opt-in` requires confirmation for every new subscription |
| Strategy | Set the address's `opt_in_required` flag and disable every report to it; the flag stays set |
| Confirmation | A subscription that needs it starts inactive and activates only through a confirmation link sent to the address |
| Existing reports on abuse | Disabled via `is_disabled`; rows stay for follow-up |
| Site message on subscribe | One generic message for everyone, so nobody can probe an address's state |
| Address matching | Trimmed and lowercased; plus-tags and Gmail dots stay distinct |
| Abuse link | One click acts immediately |
| Storage | A recipient table (approach A), not a column on `t_report` or a gateway filter |

## Data model

Migration `V93__report_recipient.sql` creates `t_report_recipient`:

| Column | Type | Notes |
|--------|------|-------|
| `id` | uuid | primary key |
| `email` | varchar(255) | normalized, unique, not null |
| `opt_in_required` | boolean | not null, default false; set when the owner reports abuse |
| `created_at` | timestamp | not null |

The same commit raises `spring.flyway.target` in `packages/server-core/src/main/resources/application-database.yaml` from 92 to 93.

`t_report` gets no foreign key and needs no backfill. Existing and new reports are matched to a recipient by `lower(trim(recipient_email))`.

Domain additions:

- `ReportRecipient(id, email, optInRequired, createdAt)`.
- `ReportRecipientRepository` port with `findByEmail(email)` and `save(recipient)`, implemented in `jpa-data`.
- `ReportRepository.disableAllByRecipientEmail(email, now)`, matching case-insensitively so it covers reports created before this change.
- A single `normalizeEmail(email)` function used by every path that stores or compares an address.

## Configuration

`app.report.subscription-mode` selects the default flow, read by `ReportUseCase` like `app.mail.sender`:

- `opt-out` (default): a new subscription is active at once unless its address has `opt_in_required` set.
- `opt-in`: every new subscription needs confirmation, whatever the flag says.

Switching the setting affects only subscriptions created afterwards.

## Flows

**Subscribe.** `ReportUseCase.createReport` normalizes the address and creates its recipient row if missing. A subscription needs confirmation when the mode is `opt-in` or the recipient has `optInRequired` set.

- No confirmation needed: the report is active and the "Dein Abo ist aktiv" mail goes out.
- Confirmation needed: the report is saved inactive (`authorized = false`) and a "Bitte bestätige dein Abo" mail goes out with a confirm link.
- The GraphQL response is identical in both cases; the `Report` type exposes only `id` and `createdAt`.

**Confirm.** `GET /reports/confirm/{reportId}?token=…` returns. The token names the report; the endpoint activates that one report and is idempotent. Only confirmation-request mails contain this link.

**Report abuse.** Every report mail carries `GET /reports/abuse/{recipientId}?token=…`: the "Dein Abo ist aktiv" mail, the confirmation request, and each weekly report. The token names the recipient, not a report, so the link keeps working after the owner cancels a report (cancel deletes the row), and no address appears in URLs or access logs. Tokens are valid for a year, like the cancel link.

One click:

1. sets the recipient's `opt_in_required` flag (a repeat click changes nothing),
2. disables every report to the address,
3. renders a page confirming that no more reports will arrive and that new subscriptions need the owner's confirmation.

Trade-off: mail security scanners (Outlook Safe Links, corporate gateways) open links automatically, so a scanner can report abuse for an address. The effect is limited to what the owner could undo by subscribing again with confirmation.

**Send.** Unchanged. The pending query already skips disabled and inactive reports.

## Surface

- `ApiUrls` gains `reportAbuse` and gets `reportConfirm` back.
- `ReportController` (http-api) serves confirm, abuse and the existing cancel endpoint; each checks that the token names the path's report or recipient.
- `SecurityConfig.whitelistedUrls()` lists `reportDelete`, `reportConfirm` and `reportAbuse` explicitly. Today only `mailForwardingAllow` is listed and there is no catch-all rule, so whether the cancel link is reachable without login depends on Spring Security's default for unmatched paths.
- `TokenIssuer` gains `createJwtForRecipient(recipientId, validForDays)`; `JwtParameterNames` gains `RECIPIENT_ID`.
- Templates: `mail-report-created` and `mail-event-calendar` get the abuse link; a new `mail-report-confirm-request`; a new page template for the abuse result.
- No GraphQL schema change.
- Frontend (`email-abo-modal`): one text for every outcome, "Wir haben dir eine E-Mail geschickt.", replacing "Dein Abo ist aktiv".

## Testing

- Domain (`ReportUseCaseTest` and a new abuse use-case test): in `opt-out` mode an unflagged address yields an active report and the active mail, a flagged one an inactive report and the confirmation request; in `opt-in` mode every address yields an inactive report; addresses normalize; reporting abuse sets the flag, disables every matching report and is idempotent.
- jpa-data: recipients are unique by normalized address; `disableAllByRecipientEmail` matches case-insensitively, including rows stored with mixed case.
- Controller: confirm and abuse reject tokens that name another report or recipient, and reject tokens that do not decode.
- Security: a test through the real filter chain shows anonymous requests reach the cancel, confirm and abuse endpoints.
- Integration (`ReportUseCaseIntTest`): after an abuse report nothing is sent, and a new subscription to the address stays inactive until confirmed.
- Freemarker tests for the changed and new templates; frontend spec for the generic message.

## Out of scope

- Hardening the cancel link against scanner clicks (a page with a POST, or RFC 8058 one-click unsubscribe).
- Acting on whoever created abusive reports (account or IP).
- Reports created before this branch that were stored inactive and would no longer be sent.
