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
- [ ] **Event-Detailseiten indexierbar machen (Block C).** Rund 1 500 eindeutige Events in 90 Tagen gegen 175 unterscheidbare Ortsseiten — knapp zehnmal so viel Fläche mit eigenem Inhalt. Kein Schema-Change nötig, `record(data:)` und der `id`-Filter existieren. Vorgelagert zu entscheiden: verlinkt die Ortsseite künftig intern statt direkt zum Veranstalter?
- [ ] **Laufzeit-Sitemap.** Gehört zu Block C: 3 000 täglich wechselnde Event-URLs sprengen den Build-Zeit-Sitemap.
- [ ] **`og:image`.** Jeder geteilte Link erscheint heute ohne Bild — in WhatsApp, Slack, LinkedIn. Für ein Produkt, dessen natürlicher Verbreitungsweg der Gruppenchat ist, direkter Reichweitenverlust. `PageTags.image` ist bereits verdrahtet, es fehlt nur die Datei.
- [ ] **Impressum als crawlbare Seite.** Existiert nur als JavaScript-Alert (`showAttribution()`), es gibt keine Route. Betreiberangaben sind ein Vertrauenssignal für Suchmaschinen und Answer-Engines — und ein Impressum, das ohne JavaScript nicht existiert, ist in der Schweiz und der EU angreifbar. Rechtlich prüfen lassen.

## 2. Bestand: ohne Inhalt hilft keine Architektur

Die Obergrenze von allem oben ist der Eventbestand. Heute: **3 094 Events in 90 Tagen (~34/Tag), davon nur die Hälfte mit eindeutigem Titel.**

- [ ] **Event-Dubletten zusammenführen.** 212 Events im 3-Tage-Fenster, nur 106 eindeutige Titel. `uniqBy(events, 'url')` fängt nur URL-Gleichheit; dieselbe Veranstaltung kommt über mehrere Quellen unter verschiedenen URLs. Verbessert jede einzelne Seite.
- [ ] **Alle kath. Kirchen hinzufügen.**
- [ ] **Alle Bibliotheken indexieren.**
- [ ] **Externe Quellen hinzufügen** (allgemein).
- [ ] **Monitoring auf Quellenausfälle.** Eine leere Ortsseite ist das Symptom eines kaputten Harvests. Bewusst *kein* `noindex` darauf — das würde genau das Warnsignal verstecken. Stattdessen: Alarm bei „Ort ohne Events seit X Tagen" und bei Quellen ohne Ertrag.
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
- [ ] Karma-Profil
- [ ] Visitenkarten

## Inhalt und Klassifikation

- [ ] **Events kategorisieren.** `packages/document-classifier` (fastText) und `categories.yaml` existieren, sind aber **nirgends in `server-core` referenziert**. Der grösste ungenutzte Baustein im Repo: Kategorien geben Filter-UI, eine zweite URL-Achse (`/events/in/CH/ZG/Zug/konzerte`) und deutlich bessere Mail-Abos auf einmal
- [ ] Konzept für Event-Tagging
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
- [ ] Klären, ob `nominatim-proxy` nach dem Wechsel auf admin.ch noch gebraucht wird

## Monetarisierung

- [ ] Twint anbinden
- [ ] Stripe/Twint-Fluss zusammenführen

## Bedienung

- [ ] Google-Calendar-Integration in der UI
- [ ] Element per Browser herunterladen
- [ ] Trigger-Sync-Button reparieren
- [ ] Präzisieren, was an Feed und ical kaputt ist — beide Endpunkte antworten (`/f/{id}/atom`, `/f/{id}/cal`); vermutlich geht es um Auffindbarkeit oder um `ics` als Formatnamen

## Qualität und Betrieb

- [ ] Lighthouse CI — existiert bisher nicht
- [ ] Express-Integrationstests. 301, 404 und Cache-Header sind heute nur als reine Funktionen getestet; die Verdrahtung wird von Hand geprüft, weil dem Projekt ein HTTP-Testharness fehlt
- [ ] `app-web`-Testlauf reparieren: 115 Suites scheitern mit `TypeError: _lruCache is not a constructor` beim Jest-Bootstrap, **bevor ein einziger Test läuft**. Damit ist `./gradlew lint test` — die Definition of Done — dauerhaft rot
- [ ] Kanton im Seitentitel doppelt: `Events in Bern (BE), BE`. Kosmetisch, aber im Titel sichtbar

## Später oder unklar

- [ ] Wochenend-Filter auf der Ortsseite
- [ ] Slugs statt prozentkodierter Ortsnamen (`Aarau%20Rohr`) — zusammen mit Block B entscheiden, beide fassen dieselben URLs an
- [ ] Individualisierte Meta-Descriptions pro Ort
- [ ] Strassenadressen für Veranstaltungsorte. `location.address` im JSON-LD führt nur Ortschaft, Kanton und Land — für Event-Rich-Results ist die Strasse der Unterschied zwischen Anzeige und Ignorieren
- [ ] Karte und `LocalBusiness`-Auszeichnung
- [ ] `llms.txt` (sinnvoll erst mit Block C, vorher gäbe es wenig zu benennen)
- [ ] Entitätssignale: `logo` und `sameAs` an der `Organization` im JSON-LD
- [ ] `hreflang`, sobald mehrsprachige Orte ausgeliefert werden — `places.ts` führt das `language`-Feld bereits
