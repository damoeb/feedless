# Tasks

Stand 2026-09-09. Teil 1 ist die vorgeschlagene Reihenfolge, Teil 2 die Sammlung. Statusangaben mit **✓** sind am Code oder an der laufenden Produktion geprüft, nicht geschätzt.

Leitfrage für die Priorisierung: **das erklärte Ziel ist organischer Traffic.** Alles, was Nachfrage abgreift oder Bestand vergrössert, steht vorn; alles, was erst bei mehr Nutzern trägt, steht hinten. Wo ein Punkt strategisch wichtig, aber nicht dringend ist, steht das dabei.

---

## Was bereits erledigt ist

Aus der Ausgangsliste — damit es nicht zweimal angefasst wird.

| Punkt | Status | Beleg |
|---|---|---|
| Sitemap überarbeiten | ✓ erledigt | 759 URLs statt 2250, Hub-Ebenen ergänzt, keine Datums-URLs mehr |
| Fix Zürich | ✓ erledigt | Kantonsname löst über admin.ch auf; `geo-resolution.spec.ts` deckt es ab |
| Mail signup/login magic link | ✓ vorhanden | `MailAuthenticationService`, `OneTimePasswordService` — Funktion prüfen, nicht neu bauen |
| Mailgun | ✓ Code vorhanden | `mail-adapter` mit `MailGatewayProperties` und Fallback-Konfiguration; offen ist nur die Prod-Konfiguration |
| Stripe | ✓ Modul vorhanden | `stripe-payments`; **Twint fehlt vollständig** (kein Treffer im Repo) |
| Feed und ical sind kaputt | ⚠ anders als beschrieben | `/f/{id}/atom` → 200, `/f/{id}/cal` → 200 mit gültigem `VCALENDAR`. Nur `/ics` → 404, das Format heisst `cal`. Der Punkt braucht eine präzise Fehlerbeschreibung, sonst ist nichts zu reparieren |
| Nominatim Schweiz | ✓ teilweise überholt | Suche und Ortsauflösung laufen bereits über `api3.geo.admin.ch`; `nominatim-proxy` existiert als eigenes Paket. Frage ist, ob es noch gebraucht wird |
| User/Groups und Rollen | ✓ teilweise | `userGroup/RoleInGroup.kt` existiert. Die flache Struktur ist der offene Teil |

---

# Teil 1 — Priorisiert

## 1. Wachstum: die begonnene SEO-Arbeit zu Ende bringen

Block A der URL-Konsolidierung ist umgesetzt und wartet auf den Merge. Die folgenden Punkte hängen direkt daran.

- [ ] **Block A mergen und in der Search Console beobachten.** Nach dem 301 auf ~2247 URLs wird es einige Wochen unruhig; der Traffic kann sinken, bevor er steigt. Nicht vorschnell gegensteuern.
- [ ] **Canonical aus dem aufgelösten Ort bauen, nicht aus dem Request-Pfad.** `/events/in/CH/Zürich/Hedingen` und `/events/in/CH/ZH/Hedingen` liefern denselben Inhalt und zeigen beide auf sich selbst. Bekanntes Loch im Canonical-Mechanismus aus Block A.
- [ ] **Event-Detailseiten wieder einschalten.** Gebaut und abgeschaltet: `EVENT_DETAIL_PAGES_ENABLED` in `apps/upcoming/src/app/feature-flags.ts` steht auf `false`, die Liste verlinkt bis auf Weiteres direkt zur Quelle. Route, Resolver, Seite und die Weiterleitungen der alten Deeplinks bleiben bestehen — es hängt nur die Verlinkung aus der Liste an dem Schalter. Der Grund ist der Bestand, nicht die Seite: über 1265 Events der nächsten 30 Tage tragen 45,5 % ausser dem Titel keinen eigenen Text, 95,9 % starten um 10:00 (ein Default der Pipeline), 0 % haben ein Bild, und bei einem Teil steckt der Text bereits im Titelfeld, weil Titel und abgeschnittener Fliesstext beim Scrapen zusammengezogen wurden. **Umlegen, sobald zwei Dinge stimmen:** die Pipeline liefert Beginn, Ende, Ort und Beschreibung als eigene Felder (siehe „Custom Attributes"), und Nutzer können Events korrigieren und ergänzen (siehe „Events bearbeiten"). Bis dahin schickt eine eigene Seite Nutzer über einen Zwischenhalt, der ihnen nichts gibt.
- [ ] **~~Event-Detailseiten indexierbar machen (Block C)~~ — gebaut, siehe oben.** Ursprüngliche Begründung: Rund 1 500 eindeutige Events in 90 Tagen gegen 175 unterscheidbare Ortsseiten — knapp zehnmal so viel Fläche mit eigenem Inhalt. Kein Schema-Change nötig, `record(data:)` und der `id`-Filter existieren. Vorgelagert zu entscheiden: verlinkt die Ortsseite künftig intern statt direkt zum Veranstalter?
- [ ] **Laufzeit-Sitemap.** Gehört zu Block C: 3 000 täglich wechselnde Event-URLs sprengen den Build-Zeit-Sitemap.
- [ ] **`og:image`.** Jeder geteilte Link erscheint heute ohne Bild — in WhatsApp, Slack, LinkedIn. Für ein Produkt, dessen natürlicher Verbreitungsweg der Gruppenchat ist, direkter Reichweitenverlust. `PageTags.image` ist bereits verdrahtet, es fehlt nur die Datei.
- [ ] **Impressum als crawlbare Seite.** Existiert nur als JavaScript-Alert (`showAttribution()`), es gibt keine Route. Betreiberangaben sind ein Vertrauenssignal für Suchmaschinen und Answer-Engines — und ein Impressum, das ohne JavaScript nicht existiert, ist in der Schweiz und der EU angreifbar. Rechtlich prüfen lassen.

## 2. Bestand: ohne Inhalt hilft keine Architektur

Die Obergrenze von allem oben ist der Eventbestand. Heute: **3 094 Events in 90 Tagen (~34/Tag), davon nur die Hälfte mit eindeutigem Titel.**

- [ ] **Event-Dubletten zusammenführen.** 212 Events im 3-Tage-Fenster, nur 106 eindeutige Titel. `uniqBy(events, 'url')` fängt nur URL-Gleichheit; dieselbe Veranstaltung kommt über mehrere Quellen unter verschiedenen URLs. Verbessert jede einzelne Seite.
- [ ] **Alle kath. Kirchen hinzufügen.**
- [ ] **Alle Bibliotheken indexieren.**
- [ ] **Externe Quellen hinzufügen** (allgemein).
- [ ] **Kaputte Quellen finden und reparieren.** In Arbeit auf `feature/feed-ctl` (Plan: `docs/superpowers/plans/2026-09-10-feedctl-and-scoped-secrets.md`): `feedctl source list --errored` findet Quellen mit wiederholten Fehlern über alle Repos (`GET /user/sources?minErrorsInSuccession=N`), `feedctl harvest view --log` zeigt den Grund, `feedctl source run --dry-run --flow fix.json` testet eine Korrektur, ohne sie zu speichern, und `feedctl source update --editor` speichert sie. Nebenbei behoben: Harvests meldeten bisher auch Fehlschläge als `ok`, und die Harvest-Liste war unsortiert.
- [ ] **Monitoring auf Quellenausfälle.** Eine leere Ortsseite ist das Symptom eines kaputten Harvests. Bewusst *kein* `noindex` darauf — das würde genau das Warnsignal verstecken. Stattdessen: Alarm bei „Ort ohne Events seit X Tagen" und bei Quellen ohne Ertrag. Datenbasis ab `feature/feed-ctl`: `errorsInSuccession` pro Quelle und `GET /user/sources?minErrorsInSuccession=N`.
- [ ] **LLM parst Daten.** Datumsextraktion ist der häufigste Grund, warum ein Event unbrauchbar ankommt.
- [ ] **Orts-Cluster neu vermessen** (Block B). 312 der 487 befüllten Ortsseiten duplizieren heute exakt eine andere, weil der 10-km-Radius fix ist. Bewusst zurückgestellt, bis die Quellen überarbeitet sind — eine Anker-Regel auf heutigen Zahlen wäre auf Sand gebaut.

## 3. Bindung: aus Besuchern Wiederkehrer machen

Erst sinnvoll, wenn oben Bestand und Fläche stimmen — aber der günstigste Hebel, sobald sie stimmen.

- [ ] **Wöchentliche Mail-Zusammenfassung nach Profil.**
- [ ] **Mailgun auf Prod konfigurieren.** Voraussetzung für alles Mail-basierte. Code ist da, es fehlt die Konfiguration.
- [ ] **Subscription-Profil** — was jemand abonniert hat, sichtbar und änderbar.
- [ ] **Abo einer bestimmten Quelle.**
- [ ] **Analytics.** Ohne Zahlen ist jede Priorisierung hier geraten. `plausible-adapter` ist ein leerer Stub — entweder füllen oder eine fertige Lösung einbinden.

## 4. Beteiligung: Angebot von aussen

- [ ] **Formular für Link-Einreichung.** Der niedrigschwelligste Weg, Bestand zu bekommen.
- [ ] **Formular für Veranstaltungsreihen**, im Stil eines YouTube-Uploads.
- [ ] **Events bearbeiten.** Braucht: Dokument mit Custom Properties, Dokument-/Event-Revisionen.
- [ ] **Custom Attributes für strukturierte Event-Angaben.** Ein Event soll `startDateTime`, `endDateTime`, Veranstaltungsort und weitere Felder explizit tragen können, statt sie aus Titel und Fliesstext raten zu müssen. Das ist die Voraussetzung für mehrere Dinge, die heute nicht gehen: eine Detailseite, die Beginn, Ende und Ort verlässlich anzeigt; `endDate` und `location.address` mit Strasse im JSON-LD, ohne die Google keine Event-Rich-Results ausspielt; und ein ICS-Export, der einen echten Zeitraum statt eines Tagesbuckets exportiert. Betrifft Datenmodell, Editor und die Einreichungsformulare gemeinsam.
- [ ] **Event-Detailansicht** (rechte Seite) inklusive Revisionen.

---

# Teil 2 — Backlog

## Community

- [ ] Twint-Beitrag (5 CHF), um eine Community zu gründen — **Twint fehlt technisch vollständig**, während Stripe als Modul existiert
- [ ] Community hat Organisator
- [ ] **Veranstalter als eigene Community**, wie ein YouTube-Kanal. Hängt mit „Community hat Organisator" zusammen.
- [ ] Karma-Profil
- [ ] Visitenkarten

## Inhalt und Klassifikation

- [ ] **Events kategorisieren.** `packages/document-classifier` (fastText) und `categories.yaml` existieren, sind aber **nirgends in `server-core` referenziert**. Der grösste ungenutzte Baustein im Repo: Kategorien geben Filter-UI, eine zweite URL-Achse (`/events/in/CH/ZG/Zug/konzerte`) und deutlich bessere Mail-Abos auf einmal
- [ ] Konzept für Event-Tagging
- [ ] **Veranstaltungsorte als eigene Entities** statt Freitext im Event. Grundlage für „Strassenadressen für Veranstaltungsorte" und den Veranstaltungsort in den Custom Attributes.
- [ ] **Redaktioneller Inhalt**, z. B. „Die besten Cafés in Wollishofen".
- [ ] Klassifikations-Plugin in die Pipeline hängen
- [ ] Summary-Plugin → GenAI-Plugin

## Plattform und Architektur

- [ ] Berechtigungskonzept als Baum statt flach, wie Google es macht
- [ ] Transformer-Refactoring, inklusive Aufbereitung von RSS-Feed-Items
- [ ] Konzept für Selector-Templates
- [ ] Quellen-Template beim Forken
- [ ] Sync-Trigger als eigene Entität
- [ ] Coroutinen aus Capabilities (`capability/SecurityContextCapabilityService.kt` ist bisher die einzige Datei dort)
- [ ] Agent-Subscriptions auf eine Message Queue umstellen
- [ ] **Profil-Gating vereinheitlichen.** `PlanGuard` hängt an `AppLayer.repository`, `UserUseCase` und `UserSecretUseCase` an `service & repository`, `IpThrottleService` nutzt `&&`, `HttpService`, `PdfService`, `MessageService` und `PageInspectionService` haben gar kein Profil. Bewusst nicht Teil des hexagonalen Umbaus (Spec `docs/superpowers/specs/2026-09-12-hexagonal-modules-design.md`).
- [ ] **`LinceseResolver` umbenennen** und `LicenseUseCase` in Provider und Use Case aufteilen (steht als `todo` im Code).
- [ ] **`TestingEndpoint` aus `server-core` lösen** — reines Dev-Werkzeug, bleibt beim hexagonalen Umbau vorerst in `server-core`.
- [ ] Klären, ob `nominatim-proxy` nach dem Wechsel auf admin.ch noch gebraucht wird

## feedctl und HTTP-API

Plan: `docs/superpowers/plans/2026-09-10-feedctl-and-scoped-secrets.md` auf `feature/feed-ctl`. Teil 1 (Broken-Source-Loop) und Teil 2 (Repos, Records, Sources) werden dort umgesetzt und zusammen gemergt; die Punkte hier sind das, was danach offen bleibt.

- [ ] **Teil 3: `plan`, `group`, `member` in `feedctl`**, dazu `PATCH /groups/{id}` und ETag/If-Match für Groups.
- [ ] **Teil 4: Scoped Secrets.** Tokens mit Scope (eine Group oder ausgewählte Repos, Rechte pro Entity, Pflicht-Ablaufdatum, `fdl_`-Präfix, nur der Hash gespeichert), verwaltet nur in der Web-UI über GraphQL — `/api/v1` bekommt keine Endpunkte für Secrets, ein Token soll keine Tokens verwalten. Anlegen und Löschen verlangt eine erneute Bestätigung je nach Anmeldeart (Root-Key, Einmal-Code per Mail, frischer SSO-Login), gültig 10 Minuten pro Session — auch für die heutigen unscoped Secrets. Danach akzeptiert `/api/v1` keine Session- und alten `UserSecret`-JWTs mehr; Agents brauchen vorher einen eigenen Scope. Secrets sind unveränderlich: Scope oder Laufzeit ändern heisst löschen und neu anlegen, beides mit Bestätigung.
- [ ] **Schreibrechte von Group-Editoren angleichen.** Der `RepositoryAccessGuard` auf `/api/v1` lässt Group-Mitglieder mit Rolle `editor` schreiben, `server-core` lehnt Update und Löschen von Repos aber ab, wenn der Aufrufer nicht Owner ist, und bei Sources, wenn seine Default-Group nicht die des Repos ist.
- [ ] **`GET /repositories` liefert je nach Filter eine andere Menge.** Ohne Filter kommen die eigenen und alle fremden öffentlichen Repos, mit `product`/`visibility`/`q` nur die eigenen (der Owner-Filter sitzt im `where`, das ohne Filter `null` ist); Repos der eigenen Groups fehlen in beiden Fällen. Eine Regel festlegen, z. B. wie GitHubs `GET /user/repos`: eigene und Group-Repos.
- [ ] **Account-Status in `feedctl status`.** Mit Login zusätzlich Angaben zum eigenen Account (z. B. Anzahl Repos, Plan) — offen, was genau.
- [ ] **Source in ein anderes Repo verschieben.** Gibt es nicht; heute nur neu anlegen und alte löschen, dabei geht die Harvest-Historie verloren.
- [ ] **`feedctl` in CI ohne `hosts.yml`.** `FEEDCTL_HOST` und `FEEDCTL_TOKEN` allein reichen nicht; CI muss `auth login` ausführen und schreibt das Token dann im Klartext in eine Datei.
- [ ] **`feedctl --host` normalisieren** (Schema, Gross-/Kleinschreibung) — heute ergibt `--host https://…` „not logged in".
- [ ] **`feedctl source run` bei vorübergehenden Netzwerkfehlern.** Das Polling bricht mit Exit 1 ab, ohne Hinweis, dass der Harvest auf dem Server weiterläuft — nicht von einem fehlgeschlagenen Lauf zu unterscheiden. Retry oder Hinweis entscheiden.
- [ ] **Harvests über 30 Minuten** können auf einer anderen Instanz kurz als fehlgeschlagen erscheinen (Sweep hängender Läufe), bevor das echte Ergebnis eintrifft.
- [ ] **GraphQL `RepositoryResolver.sources` beachtet `order` seit dem Pagination-Fix** auf `feature/feed-ctl`, und die *Standard*-Reihenfolge ist eine andere (vorher pro Seite nach `lastRecordsRetrieved`, jetzt `createdAt desc`) — prüfen, ob die Web-UI eine bestimmte Reihenfolge erwartet.
- [ ] **`/cli/install.sh` absichern**: Test, dass der Controller vor der statischen Datei am selben Pfad gewinnt; optional signieren (cosign/minisign).
- [ ] **`install.sh`-Fehlerfälle.** Scheitert `curl`, bricht das Skript ohne eigene Meldung ab; eine `http`-Basis-URL wird akzeptiert, obwohl `SHA256SUMS` vom selben Host kommt.
- [ ] **Fehlende Dateien unter `/cli/**` antworten 400.** Auf einem echten Container beantwortet der Core eine fehlende statische Datei unter `/cli/**` mit HTTP 400 und einem Body, der `"status":404` meldet („No static resource …"); README und Testkommentare (`SecurityConfigIntTest`, `CliInstallScriptController`) sprechen von 404. Status korrigieren oder die Doku anpassen.
- [ ] **`getHarvestLogs` legt `produces=text/plain` fest.**
- [ ] **`/user/sources` joint `FetchActionEntity` direkt:** Sources mit zwei Fetch-Actions erscheinen doppelt, solche ohne fehlen — ein `EXISTS` nur für `like` verwenden.
- [ ] **`GET /repositories/{id}`** liefert `retention` und `pushNotificationsMuted` nicht.
- [ ] **Group-Endpunkte** antworten Nicht-Mitgliedern mit 403 (`findByIdForUser`) und mit 500, wenn ein bestehendes Mitglied nochmals hinzugefügt wird.
- [ ] **Letzter-Owner-Prüfung ohne Sperre.** Die Prüfungen beim Löschen einer Group und beim Entfernen eines Mitglieds sperren keine Zeilen: Zwei gleichzeitige Anfragen können beide durchgehen, etwa zwei Löschungen der einzigen zwei eigenen Groups eines Users — der ist danach ausgesperrt, bis Root es repariert. `SELECT … FOR UPDATE` auf die Zuordnungen der betroffenen User.
- [ ] **`t_plan.group_id` ohne Fremdschlüssel.** Nach dem Löschen einer Group kann ihr Plan auf eine nicht mehr existierende Group zeigen; heute liest das niemand (Pläne werden pro User gesucht), beim Umbau auf Group-Pläne aber schon.
- [ ] **500-Antworten von `/api/v1` ignorieren die Request-`corrId`.** `JwtRequestFilter` setzt sie (oder übernimmt `x-corr-id`), `HttpApiExceptionHandler` erzeugt trotzdem eine neue — das Request-Attribut lesen, neue ID nur als Rückfall. Der Zweig, der Springs eigene 500er vereinheitlicht, ist ungetestet.
- [ ] **`@PreAuthorize`-Ablehnungen antworten 401 statt 403** — `feedctl` schlägt dann ein Login vor.
- [ ] **`HttpExceptionHandler`** importiert `kotlin.io.AccessDeniedException` und bildet jede Exception auf 404 ab (bestehend).
- [ ] **Authentifizierung bei Datenbankausfall:** Scheitert die Prüfung der Group-Ownership (DB weg), antwortet die Anfrage mit 401.
- [ ] **`SessionService.injectCapabilitiesFromJwt`** (Löschlink im Report) baut einen Request-Kontext ohne die erneute Group-Prüfung; `groupId.first()` wirft bei Tokens ohne Group.
- [ ] **GraphQL bildet `NoActingGroupException` auf `UNKNOWN` ab.**
- [ ] **`enableSaasProduct` läuft beim Signup ohne Group-Kontext** (bestehend).
- [ ] **Harvest-Executor:** erwartete Claim-Konkurrenz wird als ERROR geloggt; der `DataIntegrityViolationException`-Catch ist zu breit (auf den Indexnamen aus V91 prüfen); der Fehlerpfad kann ein bereits gespeichertes Scrape-Log verwerfen; eine Source, die während der Warteschlange gelöscht wird, loggt einen FK-Fehler; die Parallelität läuft auf einem einzigen `runBlocking`-Thread; echte On-Demand-Läufe erhöhen `lastUpdatedAt` nicht (danach wählt `DocumentUseCase` aus).
- [ ] **`SourceUseCase.updateSources`** speichert noch eine geladene Kopie der Source (kann den Fehlerzustand des Harvests überschreiben).
- [ ] **`feedctl`-Kleinigkeiten:** Keyring „unavailable" behandelt jeden `net.OpError` als fehlenden Keyring (auf Dial-Fehler beschränken); `auth logout` meldet Erfolg, auch wenn das Löschen im Keyring scheiterte; Login-Fehler ausser 401 enden mit Exit 4; `hosts.yml` wird nicht atomar geschrieben; Tokens haben keine `String()`-Redaktion; `auth status` endet ohne Hosts mit Exit 0; `Paginate` schützt nicht vor `hasMore` bei null Einträgen; `launchSystemEditor` ist ungetestet.
- [ ] **`JwtTokenIssuer` schreibt `exp`/`iat` in Millisekunden**, Spring/Nimbus lesen Sekunden — das Max-Age des Session-Cookies ist dadurch bedeutungslos. `JwtTokenIssuerTest` hält das heutige Verhalten fest (bestehend, beim hexagonalen Umbau gefunden).
- [ ] **`FetchActionMapper.toDomain` setzt `isVariable`, `isMobile` und `isLandscape` fest auf `false`** (MapStruct ordnet die `is…`-Properties nicht zu, `unmappedTargetPolicy = IGNORE` verschweigt es), die Flags überleben die Persistenz also nie. `ScrapeActionPlacement.placedAt` und `ScrapeActionPlacementTest` bilden das nach — Mapper, `placedAt` und Test gemeinsam korrigieren (bestehend, beim hexagonalen Umbau gefunden).

## Monetarisierung

- [ ] Twint anbinden
- [ ] Stripe/Twint-Fluss zusammenführen

## Bedienung

- [ ] Google-Calendar-Integration in der UI
- [ ] Element per Browser herunterladen
- [ ] Trigger-Sync-Button reparieren — `POST /api/v1/repositories/{r}/sources/{s}/harvests` (auf `feature/feed-ctl`) startet einen Harvest sofort und liefert eine abfragbare Harvest-ID; der Button kann darauf aufbauen
- [ ] Präzisieren, was an Feed und ical kaputt ist — beide Endpunkte antworten (`/f/{id}/atom`, `/f/{id}/cal`); vermutlich geht es um Auffindbarkeit oder um `ics` als Formatnamen

## Qualität und Betrieb

- [ ] Lighthouse CI — existiert bisher nicht
- [ ] Express-Integrationstests. 301, 404 und Cache-Header sind heute nur als reine Funktionen getestet; die Verdrahtung wird von Hand geprüft, weil dem Projekt ein HTTP-Testharness fehlt
- [ ] `app-web`-Testlauf reparieren: 115 Suites scheitern mit `TypeError: _lruCache is not a constructor` beim Jest-Bootstrap, **bevor ein einziger Test läuft**. Damit ist `./gradlew lint test` — die Definition of Done — dauerhaft rot
- [ ] Kanton im Seitentitel doppelt: `Events in Bern (BE), BE`. Kosmetisch, aber im Titel sichtbar
- [ ] **Flyway-Migrationen in Tests.** Die `server-core`-Tests bauen das Schema mit `ddl-auto=create` und lassen Flyway aus; neue Migrationen werden nur von Hand gegen PostGIS geprüft. Das Test-`import.sql` dupliziert zudem den Index aus V91
- [ ] **`feedctl`-End-to-End-Test in CI.** `:packages:cli:e2eTest` braucht gebaute Images und ist deshalb nicht Teil von `./gradlew test`; ausserdem bleibt der Start der Container unter Docker gelegentlich hängen
- [ ] **`./gradlew lint` in `app-web`** ist `prettier --write .` und verändert Dateien
- [ ] **Image-Tasks im Git-Worktree.** `buildAmdDockerImage` und `:packages:agent:bundle` lesen `grgit.head()`, das in einem Worktree `null` ist — Images lassen sich dort nur direkt mit `docker build` bauen. Das `server-core`-Image braucht dabei `--build-context cli=../cli` (seine Go-Stage baut `feedctl` daraus) und startet nur, wenn `APP_VERSION`, `APP_BUILD_TIMESTAMP` und `APP_GIT_COMMIT` als Build-Argumente gesetzt sind; die Befehlszeile steht in `packages/cli/README.md`, Defaults im Dockerfile fehlen weiterhin
- [ ] **Release-Build ohne Docker.** `scripts/build.sh` führt `./gradlew bundle` in `amazoncorretto:24` aus, ohne Docker-CLI und ohne Docker-Socket — `bundle` kann dort kein Image bauen. Klären, wie Releases tatsächlich gebaut werden

## Später oder unklar

- [ ] Wochenend-Filter auf der Ortsseite
- [ ] Slugs statt prozentkodierter Ortsnamen (`Aarau%20Rohr`) — zusammen mit Block B entscheiden, beide fassen dieselben URLs an
- [ ] Individualisierte Meta-Descriptions pro Ort
- [ ] Strassenadressen für Veranstaltungsorte. `location.address` im JSON-LD führt nur Ortschaft, Kanton und Land — für Event-Rich-Results ist die Strasse der Unterschied zwischen Anzeige und Ignorieren
- [ ] Karte und `LocalBusiness`-Auszeichnung
- [ ] `llms.txt` (sinnvoll erst mit Block C, vorher gäbe es wenig zu benennen)
- [ ] Entitätssignale: `logo` und `sameAs` an der `Organization` im JSON-LD
- [ ] `hreflang`, sobald mehrsprachige Orte ausgeliefert werden — `places.ts` führt das `language`-Feld bereits
