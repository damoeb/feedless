# URL-Konsolidierung und Event-Seiten für `upcoming` / lokale.events

**Datum:** 2026-09-09
**Modul:** `packages/frontend/apps/upcoming`
**Ziel:** Maximierung des organischen Traffics.

Die Arbeit zerfällt in drei Blöcke. **A** beseitigt Schaden und legt das Fundament, **B** ist bis nach dem Source-Fix vertagt, **C** ist der eigentliche Wachstumsschritt. A kommt vor C, weil die heutigen Event-URLs unter den Datums-Pfaden hängen, die A entfernt.

## Ausgangslage

Alle Angaben stammen aus dem Code sowie aus Live-Abfragen von https://lokale.events und `https://api.lokale.events/graphql` am 2026-09-09.

### Was funktioniert

Die Datums-Seiten sind solide gebaut: eigener Titel inklusive PLZ (`Events Heute in 6300, Zug, ZG | lokale.events`), ein `<h1>`, sieben bis zehn interne Links und ein korrekter `ld+json`-Block mit `WebPage` → `ItemList` → `Event`-Knoten, jeweils mit echtem `startDate`, `PostalAddress` und `GeoCoordinates`, dazu eine `BreadcrumbList`. An der strukturierten Auszeichnung ist nichts zu tun.

### Was nicht funktioniert

**776 indexierbare Seiten sind leer.** Drei Routen-Ebenen sind registriert, liefern aber nichts aus: `/events/in/CH`, `/events/in/CH/ZG` und die Ortsseite ohne Datum `/events/in/CH/ZG/Zug`. Alle drei antworten mit HTTP 200, ohne `<h1>`, ohne einen einzigen Link auf `/events/in/`, ohne JSON-LD, mit dem generischen Fallback-Titel „Veranstaltungen in deiner Nähe entdecken", Canonical auf sich selbst und `robots: index, follow`. Ursache ist `event-calendar.page.html:12`: `@if (namedLatLon)` gated den kompletten Body, und ohne `place`-Parameter wird `namedLatLon` nie gesetzt. Aus Suchmaschinensicht sind das Soft-404s mit identischem Titel — ein Qualitätssignal gegen die gesamte Domain. Die ursprüngliche Absicht, Crawlern das Auffinden aller Orte zu ermöglichen, ist damit nicht nur unwirksam, sondern kostet aktiv.

**Die Datums-URLs kannibalisieren sich.** `getDateConstraints` (`event-calendar.page.ts:130`) liefert ein Fenster von `date … date+2`. `/heute` zeigt heute bis übermorgen, `/morgen` zeigt morgen bis überübermorgen — eine Überlappung von zwei der drei Tage. Pro Ort existieren drei weitgehend inhaltsgleiche URLs, die um dieselbe Suchanfrage konkurrieren; die indexierbare Fläche von 749 × 3 = 2247 URLs enthält keine einzige inhaltlich stabile.

**Toter Code.** `checkOutdated` in `server-utils.ts` löst nie aus: `/CH/ZG/Zug/am/2026/09/08` und `/am/2026/01/05` antworten beide mit 200 und ohne Redirect, `safeParsePath` greift dort nicht. Zusätzlich ist die Bedingung invertiert (`routeDate.isAfter(maxAge)` trifft auf *frische* Daten zu) und das Redirect-Ziel wäre die aufgerufene URL selbst gewesen.

### Gemessener Bestand

| Messung | Wert |
|---|---|
| Events in den nächsten 90 Tagen | 3 094 (~34/Tag) |
| Events im 3-Tage-Fenster | 212 |
| davon eindeutige Titel | 106 |
| Orte laut Sitemap | 749 |
| Ortsseiten mit ≥1 Event im 10-km-Radius | 487 (66 %) |
| davon inhaltlich verschieden | **175** |
| Ortsseiten, die eine andere exakt duplizieren | **312** |
| Ortsseiten ohne Events | 253 (34 %) |

Zwei Zahlen bestimmen alles Weitere.

**312 der 487 befüllten Ortsseiten sind exakte Duplikate einer anderen Seite.** 18 Orte im Aargau — Mägenwil, Mellingen, Wohlenschwil, Hendschiken, Birr, Birrhard und weitere — liefern dieselbe Eventliste, weil sie alle im 10-km-Radius derselben Events liegen. Im Rheintal sind es acht Orte mit identischen 102 Events. Effektiv existieren nicht 749 unterscheidbare Ortsseiten, sondern 175.

**Von 212 Events im Fenster haben nur 106 einen eindeutigen Titel.** Die Hälfte des Bestands sind Dubletten, die `uniqBy(events, 'url')` in `EventService` nicht fängt, weil dieselbe Veranstaltung über verschiedene Quellen unter verschiedenen URLs ankommt.

**Daraus folgt die Erwartungshaltung an diese Spec.** Block A entfernt Bremsen, erzeugt aber keine Nachfrage: er beseitigt Soft-404-Drag, beendet Selbstkannibalisierung und lässt Crawling erstmals funktionieren. Der Wachstumsschritt ist Block C, weil rund 1 500 eindeutige Events in 90 Tagen fast zehnmal so viel Seitenfläche mit echtem eigenem Inhalt darstellen wie 175 unterscheidbare Ortsseiten — und jeder Eventtitel eine Longtail-Query ohne Wettbewerb ist.

### Nebenbefund

Die API deckelt anonyme Requests bei **30 Records pro Seite**, unabhängig vom angefragten `pageSize`. `EventService.fetchEventsBetweenDates` fragt mit `pageSize: 50` an und bekommt nie mehr als 30. Beim heutigen 10-km-Radius über drei Tage greift das selten, bei größerem Bestand oder größerem Fenster schneidet es still ab. Nicht Teil dieser Spec, aber vor einer Fensteränderung zu prüfen.

### Struktureller Zustand

Gemessen an `/events/in/CH/ZG/Zug/heute` und der Domain-Konfiguration.

**Gut:** SSR liefert vollständigen Inhalt ohne JavaScript. Brotli komprimiert 55 KB auf 8,9 KB, nur zwei externe JS-Dateien. Canonicals sitzen, auch die Trailing-Slash-Variante `/heute/` zeigt korrekt auf die Version ohne Slash. `www` → non-www ist ein sauberer 301. OG- und Twitter-Tags sind vorhanden. `robots.txt` sperrt keine KI-Crawler aus.

**Mängel, nach Schwere:**

| # | Befund | Belegt durch |
|---|---|---|
| 1 | **Unbegrenzter Soft-404-Raum.** `/events/in/CH/XX/Nirgendwo/heute` und `/events/in/XX` antworten mit **200**. Jede erfundene Orts-URL liefert eine leere Seite mit Erfolgsstatus; nur `/gibtesnicht` ergibt korrekt 404. | Live-Abruf |
| 2 | **Überschriften-Hierarchie invertiert.** 1× h1, 8× h2, 17× h3 — aber sieben h3 stehen *vor* dem h1. Die Gliederung beginnt bei Ebene drei. | Live-Abruf |
| 3 | **Keine Cache-Header.** Weder `Cache-Control` noch `ETag` noch `Last-Modified`. Jeder Bot-Hit hängt am SSR, jeder Crawl lädt alles neu. | Response-Header |
| 4 | **Versteckter Text für Bots.** 33 Vorkommen von `.bot-only { display: none }` mit `itemprop`-Inhalten. Redundant zum JSON-LD, das dieselben Daten bereits vollständig führt. | Live-Abruf |
| 5 | **Kein `og:image`.** Geteilte Links erscheinen in WhatsApp, Slack, Facebook und LinkedIn ohne Bild. | Live-Abruf |
| 6 | **Uhrzeiten unsichtbar.** Sichtbar sind Titel, Textanfang und Ortschaft; `startDate` steht ausschließlich im versteckten `<meta>`. | Live-Abruf |
| 7 | **Falsche Locale-Signale.** `og:locale` ist `de_DE` statt `de_CH`, `<html lang="de">` statt `de-CH`, kein `hreflang`. `places.ts` führt bereits ein `language`-Feld. | Live-Abruf |
| 8 | **Kein `llms.txt`, kein Web-Manifest.** | 404 auf beiden |

**GEO-spezifisch.** Die Voraussetzungen stimmen — serverseitiger Inhalt, korrektes JSON-LD, kein ausgesperrter Crawler. Drei Dinge fehlen strukturell: *Antwortfähigkeit* (eine Answer-Engine braucht was/wann/wo/Preis; sichtbar sind nur was und eine grobe Ortschaft), *Zitierbarkeit* (jedes Event verlinkt nach außen, es gibt keine eigene URL, die als Quelle taugt — das löst Block C), und *Entitätssignale* (die `Organization` im JSON-LD hat weder `logo` noch `sameAs`, und das Impressum existiert nur als JavaScript-Alert in `showAttribution()`, also ohne crawlbare Seite).

## Entscheidungen

| Frage | Entscheidung |
|---|---|
| Rolle der Ortsseite | Evergreen-Hauptseite, Default heute |
| Datum in der URL | Query-Parameter `?date=`, nicht im Pfad |
| `/heute`, `/morgen`, `/kommendes-wochenende` | 301 auf die Ortsseite |
| Wochenend-Intention | Filter auf der Ortsseite, keine eigene URL |
| Migrationsstrategie | Sauberer Schnitt mit 301, nicht schrittweise |
| Zeitfenster | 3 Tage, unverändert |
| Orte ohne Events | bleiben indexierbar, **kein** `noindex` |
| Orts-Cluster (Block B) | vertagt bis nach dem Source-Fix, dann neu messen |

**Zu den leeren Ortsseiten:** Eine leere Seite ist das Symptom eines kaputten Harvests, keine Eigenschaft des Ortes. Ein `noindex` zur Renderzeit würde genau das Signal verstecken, das warnen soll — die Seite verschwände still aus dem Index, statt dass auffällt, dass eine Quelle nichts mehr liefert. Die richtige Antwort ist Monitoring auf „Ort ohne Events seit X Tagen", nicht eine SEO-Regel. Bewusst in Kauf genommen: solange Orte leer sind, bleibt für sie ein Soft-404-Risiko bestehen.

# Block A — URL-Konsolidierung

## Zielbild

```
/events/in/CH                          Hub: alle Kantone
/events/in/CH/ZG                       Hub: alle Orte des Kantons
/events/in/CH/ZG/Zug                   Hauptseite, Default heute   ← canonical
/events/in/CH/ZG/Zug?date=2026-09-12   teilbare Tagesansicht        → noindex, canonical auf Basis
/events/in/CH/ZG/Zug?event=<id>        Deeplink ins Detail-Modal    → noindex, canonical auf Basis
```

Aus 2247 volatilen, sich gegenseitig kannibalisierenden URLs werden 776 stabile. Die Autorität sammelt sich auf einer URL pro Ort, statt sich auf drei zu verteilen. Der Inhalt wechselt täglich, die Adresse nicht — dieselbe Mechanik, nach der Wetter- oder Kinoprogramm-Seiten ranken.

## Routing

Die Zweige `dateTime` und `relativeDateTime` entfallen aus `upcomingBaseRoute` (`upcoming-product-routes.ts`) und aus `app.routes.server.ts`. Damit verschwinden auch `parseDateFromUrl`, `parseRelativeDate`, `relativeDateParser` und die Verzweigungslogik in `createDateUrl`, die heute entscheidet, ob ein Datum als Relativausdruck oder als Pfad geschrieben wird.

Das Datum kommt aus `?date=YYYY-MM-DD`; fehlt der Parameter, gilt heute. Der Event-Deeplink, heute `/…/heute/<eventId>`, wird zu `?event=<id>`; `openEventModalForId` bleibt unverändert und wird nur anders adressiert. Block C ersetzt diesen Deeplink später durch eine echte Seite.

`RenderMode.Server` bleibt für alle Routen — SSR on demand, kein Prerendering. Das tagesaktuelle Rendering der Ortsseite passt ohne Umbau hinein.

## Hub-Seiten

Zwei neue Komponenten, bewusst nicht `EventCalendarPage`: die ist mit 997 Zeilen bereits am Limit, und die Hubs brauchen weder Resolver noch GraphQL. Datenquelle ist `getCachedLocations()` aus `libs/geo/src/lib/places.ts` — statisch, damit SSR-sicher und ohne Backend-Abhängigkeit.

**`/events/in/CH`** listet alle Kantone, gruppiert über `area`, mit der Zahl der Orte je Kanton. `<h1>Veranstaltungen in der Schweiz</h1>`.

**`/events/in/CH/ZG`** listet alle Orte des Kantons alphabetisch, jeder verlinkt auf seine Ortsseite. `<h1>Veranstaltungen im Kanton Zug</h1>`.

Beide erhalten eigenen Titel, eigene Description sowie `BreadcrumbList`- und `ItemList`-JSON-LD. Damit existiert die Crawler-Kette Land → Kanton → Ort zum ersten Mal tatsächlich.

## Ortsseite

Rendert das bestehende 3-Tage-Fenster ab dem Datum aus `?date=`, ersatzweise ab heute.

Der Canonical zeigt **immer** auf die parameterlose URL. Das ist der Mechanismus, der die Signale bündelt. Bei gesetztem `?date=` oder `?event=` kommt `noindex, follow` dazu.

Titel und Description werden pro Ort unterschiedlich. Heute ist die Description über alle 749 Orte wortgleich und variiert nur im Ortsnamen; PLZ, Kanton und die nächstgelegenen Orte aus `places.ts` erlauben Individualisierung ohne Redaktionsaufwand.

## Migration

Eine Express-Middleware vor `angularApp.handle()` in `server.ts`, an der Stelle, an der heute `checkOutdated` steht:

| Alt | Neu |
|---|---|
| `/…/heute`, `/…/morgen`, `/…/kommendes-wochenende` | 301 → Ortsseite |
| `/…/am/YYYY/MM/DD` | 301 → Ortsseite `?date=YYYY-MM-DD` |
| `/…/<datum>/<eventId>` | 301 → Ortsseite `?event=<id>` |

`checkOutdated` und sein Test entfallen ersatzlos.

`robots.txt` verliert die `Disallow`-Regeln für `/am/` und die Detail-Pfade — diese URLs existieren nicht mehr. Gesteuert wird über Canonical, nicht über Crawl-Blockade; nur so kann Google die Konsolidierungssignale überhaupt lesen.

Der Sitemap-Generator (`generate-sitemaps.ts`) liefert künftig Hub- und Ortsseiten statt der Datums-URLs: 1 + 26 + 749 = 776 Einträge statt 2250. Die Hub-Ebenen fehlen dort heute vollständig.

## Strukturelle Korrekturen

Die Befunde 1 bis 7 aus dem Strukturbefund. Sie gehören in diesen Block, weil er ohnehin Routing, SSR-Server und das Seiten-Template anfasst — einzeln wären sie kaum ein eigener Vorgang wert, zusammen sind es wenige Stunden.

**Unbekannte Orte mit 404 beantworten (Befund 1).** `parseLocationFromUrl` wirft bereits, wenn ein Ort nicht auflösbar ist; heute fängt der Resolver das ab und rendert eine leere Seite. Künftig muss der SSR-Pfad in diesem Fall Status 404 setzen statt 200. Dasselbe gilt für unbekannte Kantone und Ländercodes auf den Hub-Ebenen. Das ist die wirksamste Einzelmaßnahme des ganzen Blocks: ohne sie kann Google beliebig viele wertlose URLs entdecken, die alle mit Erfolg antworten.

**Cache-Header setzen (Befund 3).** `Cache-Control: public, s-maxage=<Sekunden bis Mitternacht>` auf den gerenderten Seiten. Der Inhalt wechselt genau einmal pro Tag; ohne Header hängt jeder der 776 Crawls am SSR.

**Überschriften-Hierarchie reparieren (Befund 2).** Die sieben `h3` des Inline-Kalenders stehen vor dem `h1`. Der Kalender ist Navigation, keine Gliederung — die Elemente sollten keine Überschriften sein. Das `h1` gehört an den Anfang des Dokumentflusses.

**`bot-only` entfernen (Befund 4).** Die 33 versteckten `itemprop`-Elemente sind vollständig redundant zum JSON-LD. Ersatzloses Löschen entfernt das Richtlinienrisiko und vereinfacht das Template.

**`og:image` ergänzen (Befund 5).** Statisches Bild als Ausgangspunkt; später pro Ort oder pro Event.

**Startzeit sichtbar machen (Befund 6).** Pro Event die Uhrzeit aus `startingAt` als sichtbares `<time>`-Element rendern statt nur als verstecktes `<meta>`. Das ist zugleich der wirksamste GEO-Punkt: Answer-Engines zitieren bevorzugt Fließtext.

**Locale korrigieren (Befund 7).** `og:locale` auf `de_CH`, `<html lang="de-CH">`. `hreflang` bleibt offen, bis mehrsprachige Orte tatsächlich ausgeliefert werden.

## Tests

Betroffen sind `server.spec.ts`, `upcoming-product-routes.spec.ts`, `events-resolver.spec.ts` und `geo-resolution.spec.ts` — alle vier fassen das Routing an.

Neu: Redirect-Tests für die Migrations-Middleware, je einer pro Zeile der Tabelle oben; ein Test, dass der Sitemap-Generator Hub-Ebenen enthält und keine Datums-URLs mehr; Specs für die beiden Hub-Komponenten; ein Test, dass der Canonical bei gesetztem `?date=` auf die parameterlose URL zeigt und die Seite `noindex` trägt.

Für die strukturellen Korrekturen: ein Test, dass ein unbekannter Ort, Kanton und Ländercode je **404** liefern und kein 200; ein Test, dass gerenderte Seiten einen `Cache-Control`-Header tragen; ein Test, dass das `h1` vor jeder tieferen Überschrift steht.

Zwei dieser Tests sind nicht optional. `?date=` ist für Suchmaschinen nur solange unproblematisch, wie der Canonical sitzt — sitzt er nicht, entsteht ein unbegrenzter URL-Raum. Und der 404-Test sichert genau die Eigenschaft, deren Fehlen heute den unbegrenzten Soft-404-Raum erzeugt; ohne Test kehrt sie beim nächsten Resolver-Umbau still zurück.

# Block B — Orts-Cluster (vertagt)

312 Ortsseiten duplizieren eine andere Seite exakt, weil der 10-km-Radius fix ist und benachbarte Orte denselben Eventpool sehen. Das ist dasselbe Problem wie auf der Datums-Achse, nur größer.

Die Lösung — Canonical auf einen Cluster-Anker oder ein an die Ortsdichte gekoppelter Radius — hängt davon ab, wie die Cluster nach dem Source-Fix aussehen. Eine Anker-Regel auf heutigen Zahlen wäre auf Sand gebaut. **Neu messen, sobald die Quellen überarbeitet sind**, dann entscheiden.

# Block C — Event-Detailseiten

Rund 1 500 eindeutige Events in 90 Tagen gegen 175 unterscheidbare Ortsseiten: knapp zehnmal so viel Seitenfläche mit eigenem Inhalt. Jeder Eventtitel ist eine Longtail-Query ohne Wettbewerb. Heute blockiert durch `robots: 'noindex, follow'` in `getPageTags` (`event-calendar.page.ts:315`) und die `Disallow`-Regeln in `robots.txt`.

**Kein Schema-Change nötig.** `record(data: RecordWhereInput)` existiert in `schema.graphqls`, und `RecordsWhereInput` hat einen `id`-Filter (Zeile 613). Der auskommentierte `eventResolver` in `upcoming-product-routes.ts:170` lässt sich mit einem `EventService.findById` gegen `recordById` direkt aktivieren — kein Codegen-Lauf.

## Offene Produktentscheidung

**Verlinkt die Ortsseite künftig intern auf die Event-Seite statt nach außen?** Heute ist jedes Event ein `[href]="event.url"` direkt zum Veranstalter (`event-calendar.page.html`). Ohne interne Verlinkung finden Crawler die Event-Seiten nur über einen Sitemap; mit ihr landen Nutzer künftig erst bei dir statt beim Veranstalter. Das ist eine Produkt-, keine SEO-Frage, und sie ist **vor der Umsetzung von C zu entscheiden**, weil der nächste Abschnitt daran hängt.

## Umfang

1. `EventService.findById` gegen `recordById`; den auskommentierten Resolver aktivieren
2. Echte Seite statt Modal — beziehungsweise SSR rendert die Seite, im Browser bleibt das Modal
3. URL-Form festlegen, Vorschlag `/events/in/CH/ZG/Zug/e/<titel-slug>-<id>`
4. Meta-Tags und Canonical; das JSON-LD kann `toSchemaOrgEvent` wiederverwenden
5. `noindex` in `getPageTags` und die `Disallow`-Regeln in `robots.txt` entfernen
6. Interne Verlinkung von der Ortsseite — abhängig von der Entscheidung oben
7. Auffindbarkeit: 3 000 Events, täglich wechselnd, sprengt den Build-Zeit-Sitemap. Entweder Laufzeit-Sitemap oder Verlass auf Punkt 6
8. Umgang mit vergangenen Events: Seite bestehen lassen oder 410
9. Schutz gegen dünne Seiten bei Events ohne Text
10. `llms.txt` ergänzen (Befund 8) — die Event-Seiten sind erst mit C zitierfähig, vorher hätte die Datei wenig zu benennen
11. Entitätssignale: `logo` und `sameAs` an der `Organization` im JSON-LD

Punkt 6 und 7 sind die Brocken, 1 bis 5 sind überschaubar, 10 und 11 sind Einzeiler.

## Laufzeit-Sitemap

Der Sitemap entsteht heute zur Build-Zeit. Für 3 000 täglich wechselnde Event-URLs reicht das nicht. Ein zur Laufzeit erzeugter Sitemap ist mit einer einzigen paginierten Query machbar und löst zugleich das Problem, dass der statische Sitemap nicht weiß, welche Orte leer sind. Gehört zu C, nicht zu A.

# Nicht in diesem Umfang

- **Event-Dubletten zusammenführen.** 106 eindeutige Titel bei 212 Events. `uniqBy(events, 'url')` in `EventService` fängt nur URL-Gleichheit. Eine Deduplizierung über normalisierten Titel plus `startingAt` würde jede Seite verbessern — gehört aber zur Source-Baustelle.
- **Individualisierte Descriptions** und der **Wochenend-Filter** auf der Ortsseite.
- **Kategorien** als zweite URL-Achse. Der fastText-Klassifikator in `packages/document-classifier` und `categories.yaml` liegen ungenutzt bereit; der nächste große Hebel nach C, braucht aber Backend-Arbeit.
- **Monitoring auf leere Ortsseiten** als Harvest-Warnsignal.
- **Impressum als crawlbare Seite.** Es existiert heute nur als JavaScript-Alert (`showAttribution()` in `upcoming-footer.component.ts`), es gibt keine Route dafür. Für Suchmaschinen und Answer-Engines ist der Betreiber damit unsichtbar, und Betreiberangaben sind ein Vertrauenssignal. Unabhängig vom SEO-Nutzen rechtlich prüfen lassen: ein Impressum, das ohne JavaScript nicht existiert, ist in der Schweiz und in der EU angreifbar. Eigene, kleine Aufgabe — nicht mit dieser Spec vermischen.
- **Straßenadressen für Veranstaltungsorte.** `location.address` im JSON-LD führt nur Ortschaft, Kanton und Land. Für Event-Rich-Results ist die Straße der Unterschied zwischen Anzeige und Ignorieren. Das ist eine Datenfrage und gehört zur Source-Baustelle.
- **Karte und `LocalBusiness`-Auszeichnung** für Veranstaltungsorte.
- **Slugs statt prozent-kodierter Ortsnamen** in URLs (`Aarau%20Rohr`). Zusammen mit Block B zu entscheiden, da beide die Orts-URLs anfassen.

# Risiken

**Traffic-Delle nach der Umstellung.** Nach dem 301 wird es in der Search Console einige Wochen unruhig, und der Traffic kann sinken, bevor er steigt. Das ist bei einer URL-Konsolidierung normal und kein Zeichen eines Fehlers — es fühlt sich in Woche zwei nur so an.

**Verlust an Fläche.** 2247 indexierbare URLs werden zu 776. Der Tausch ist bewusst: Autorität bündeln statt streuen. Bei einer jungen Domain ist das die bessere Seite des Tauschs, aber es ist ein Tausch.

**Die Obergrenze setzt der Bestand, nicht die Architektur.** Solange nur 175 unterscheidbare Ortsseiten existieren, ist das die reale Decke von Block A — unabhängig davon, wie sauber er umgesetzt wird. Block C hebt diese Decke, indem er die Events selbst zu Seiten macht; der Bestand selbst wächst nur über die Quellen.

**Genauigkeit der Messung.** `recordsFrequency` meldet für dieselben drei Tage 318 Events, die Paginierung über `records` liefert 212. Die Größenordnung ist belastbar, die exakte Zahl nicht. Vor einer Entscheidung, die an der genauen Zahl hängt, nachprüfen.

# Definition of Done

`./gradlew lint test` mit Exit-Code 0.
