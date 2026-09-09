# Block A — URL-Konsolidierung Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Die Ortsseite `/events/in/CH/ZG/Zug` wird die einzige, stabile Event-URL; Datum und Event-Deeplink wandern in Query-Parameter, die Datums-Pfade werden per 301 eingeklappt, die Hub-Ebenen rendern erstmals Inhalt, und unbekannte Orte antworten mit 404 statt 200.

**Architecture:** Alle URL-Entscheidungen, die vor dem Angular-Rendering fallen müssen — 301 auf Alt-URLs und 404 auf unbekannte Orte — laufen als reine Funktionen in `server-utils.ts` und werden in `server.ts` als Express-Middleware verdrahtet. Die statische Ortsliste aus `@feedless/geo` ist dafür die einzige Wahrheitsquelle, also braucht keine dieser Entscheidungen einen Backend-Call. Innerhalb von Angular verlieren die Routen ihre Datums-Zweige; die Ortsroute bekommt den bestehenden `eventsResolver`, und das Datum kommt aus `?date=`.

**Tech Stack:** Angular 21, Nx 22, Ionic 8, Express (Angular SSR, `RenderMode.Server`), Vitest über `@angular/build:unit-test`, typesafe-routes, dayjs, Node 24.

**Spec:** `docs/superpowers/specs/2026-09-09-upcoming-url-konsolidierung-design.md`

## Global Constraints

- Arbeitsverzeichnis für alle JS-Befehle: `packages/frontend`. Alle Pfade in diesem Plan sind relativ zum Repo-Wurzelverzeichnis.
- Schnelle Testschleife: `npx nx test upcoming` (Baseline vor Beginn: 16 Dateien, 39 grün, 10 übersprungen, Exit 0, ~3 s).
- Definition of Done für den gesamten Block: `./gradlew lint test` mit Exit-Code 0.
- Branch von `develop` mit Präfix `feature/`, **nicht** von `master`. CI läuft nur auf PRs gegen `develop`.
- Conventional Commits, Scope `frontend`: `feat(frontend): …`, `fix(frontend): …`.
- Generierten Code niemals von Hand ändern: `src/generated/graphql.ts` entsteht aus `schema.graphqls`. Dieser Block ändert kein Schema und braucht keinen Codegen-Lauf.
- Ortsnamen stehen prozent-kodiert in URLs (`Aarau%20Rohr`). Jede Pfadzerlegung dekodiert, jede Pfaderzeugung kodiert. Slugs sind **nicht** Teil dieses Blocks.
- `countryCode` ist in `places.ts` durchgängig `'CH'`; die Ortsliste kennt derzeit die Kantone ZH, AG, ZG, SZ, SG, GL.
- Prosa in Markdown-Dateien wird nicht hart umbrochen.

---

### Task 1: Unbekannte Orte mit 404 beantworten

Heute antwortet `/events/in/CH/XX/Nirgendwo/heute` mit **200** und einer leeren Seite. Jede erfundene Orts-URL erzeugt damit eine indexierbare Erfolgsantwort. Das ist der unbegrenzte Soft-404-Raum aus Befund 1 der Spec und die wirksamste Einzelmaßnahme des Blocks.

Die Prüfung läuft bewusst in Express und nicht in Angular: die Ortsliste ist statisch, die Entscheidung ist eine reine Funktion, und Angular-SSR bietet keinen bequemen Weg, aus einer Komponente heraus den HTTP-Status zu setzen.

**Files:**
- Modify: `packages/frontend/apps/upcoming/src/server-utils.ts`
- Modify: `packages/frontend/apps/upcoming/src/server.ts:88-104`
- Test: `packages/frontend/apps/upcoming/src/server-utils.spec.ts` (neu)

**Interfaces:**
- Produces:
  - `type EventsPath = { countryCode: string; region?: string; place?: string; rest: string[] }`
  - `parseEventsPath(pathname: string): EventsPath | null`
  - `isKnownLocation(path: EventsPath): boolean`

- [ ] **Step 1: Write the failing test**

Neue Datei `packages/frontend/apps/upcoming/src/server-utils.spec.ts`:

```ts
import { isKnownLocation, parseEventsPath } from './server-utils';

describe('parseEventsPath', () => {
  it('returns null for paths outside /events/in', () => {
    expect(parseEventsPath('/ueber-uns')).toBeNull();
    expect(parseEventsPath('/')).toBeNull();
  });

  it('parses country, region, place and the remaining segments', () => {
    expect(parseEventsPath('/events/in/CH/ZG/Zug/heute')).toEqual({
      countryCode: 'CH',
      region: 'ZG',
      place: 'Zug',
      rest: ['heute'],
    });
  });

  it('decodes percent-encoded place names', () => {
    expect(parseEventsPath('/events/in/CH/AG/Aarau%20Rohr')?.place).toBe(
      'Aarau Rohr',
    );
  });

  it('parses the hub levels', () => {
    expect(parseEventsPath('/events/in/CH')).toEqual({
      countryCode: 'CH',
      region: undefined,
      place: undefined,
      rest: [],
    });
    expect(parseEventsPath('/events/in/CH/ZG')).toEqual({
      countryCode: 'CH',
      region: 'ZG',
      place: undefined,
      rest: [],
    });
  });
});

describe('isKnownLocation', () => {
  it('accepts the country hub', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH')!)).toBe(true);
  });

  it('accepts a known region and a known place', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH/ZG')!)).toBe(true);
    expect(isKnownLocation(parseEventsPath('/events/in/CH/ZG/Zug')!)).toBe(
      true,
    );
  });

  it('rejects an unknown country, region or place', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/XX')!)).toBe(false);
    expect(isKnownLocation(parseEventsPath('/events/in/CH/QQ')!)).toBe(false);
    expect(
      isKnownLocation(parseEventsPath('/events/in/CH/ZG/Nirgendwo')!),
    ).toBe(false);
  });

  it('ignores trailing segments when validating', () => {
    expect(
      isKnownLocation(parseEventsPath('/events/in/CH/ZG/Zug/heute')!),
    ).toBe(true);
  });

  it('matches place names case-insensitively', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH/zg/zug')!)).toBe(
      true,
    );
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `parseEventsPath` und `isKnownLocation` sind in `server-utils.ts` nicht exportiert.

- [ ] **Step 3: Write minimal implementation**

In `packages/frontend/apps/upcoming/src/server-utils.ts` ergänzen (die bestehenden Exporte bleiben in diesem Task unangetastet):

```ts
import { getCachedLocations } from '@feedless/geo';

export type EventsPath = {
  countryCode: string;
  region?: string;
  place?: string;
  rest: string[];
};

const EVENTS_PREFIX = '/events/in/';

/**
 * Zerlegt `/events/in/CH/ZG/Zug/heute` in seine Bestandteile. Liefert null für
 * jeden Pfad ausserhalb von /events/in, damit der Aufrufer ihn unverändert an
 * Angular weiterreicht.
 */
export function parseEventsPath(pathname: string): EventsPath | null {
  if (!pathname.startsWith(EVENTS_PREFIX)) {
    return null;
  }
  const segments = pathname
    .slice(EVENTS_PREFIX.length)
    .split('/')
    .filter((segment) => segment.length > 0)
    .map((segment) => decodeURIComponent(segment));

  if (segments.length === 0) {
    return null;
  }

  const [countryCode, region, place, ...rest] = segments;
  return { countryCode, region, place, rest };
}

const normalize = (value: string): string => value.trim().toLowerCase();

/**
 * Prüft Land, Kanton und Ort gegen die statische Ortsliste. Ohne diese Prüfung
 * beantwortet jede erfundene Orts-URL mit 200 und erzeugt einen unbegrenzten
 * Soft-404-Raum.
 */
export function isKnownLocation(path: EventsPath): boolean {
  const locations = getCachedLocations();
  const inCountry = locations.filter(
    (location) => normalize(location.countryCode) === normalize(path.countryCode),
  );
  if (inCountry.length === 0) {
    return false;
  }
  if (!path.region) {
    return true;
  }
  const inRegion = inCountry.filter(
    (location) => normalize(location.area) === normalize(path.region!),
  );
  if (inRegion.length === 0) {
    return false;
  }
  if (!path.place) {
    return true;
  }
  return inRegion.some(
    (location) => normalize(location.place) === normalize(path.place!),
  );
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS — die neuen Fälle grün, die 39 bestehenden weiterhin grün.

- [ ] **Step 5: Wire the 404 into express**

In `packages/frontend/apps/upcoming/src/server.ts` den Handler ergänzen. Die Prüfung steht **vor** `angularApp.handle`, aber nach `serveStatic`:

```ts
app.use('/**', (req, res, next) => {
  const eventsPath = parseEventsPath(req.path);
  if (eventsPath && !isKnownLocation(eventsPath)) {
    return res.status(404).send('Not found');
  }

  // … bestehender checkOutdated-Block und angularApp.handle bleiben vorerst
});
```

Import oben ergänzen:

```ts
import {
  checkOutdated,
  createAccessLogLine,
  isKnownLocation,
  parseEventsPath,
} from './server-utils';
```

- [ ] **Step 6: Verify manually against the dev server**

Run: `cd packages/frontend && npx nx serve apps:upcoming`
Dann in einem zweiten Terminal:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:4200/events/in/CH/ZG/Zug
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:4200/events/in/CH/XX/Nirgendwo/heute
```

Expected: `200` für den bekannten Ort, `404` für den unbekannten.

- [ ] **Step 7: Commit**

```bash
git add packages/frontend/apps/upcoming/src/server-utils.ts \
        packages/frontend/apps/upcoming/src/server-utils.spec.ts \
        packages/frontend/apps/upcoming/src/server.ts
git commit -m "fix(frontend): answer unknown places with 404 instead of 200"
```

---

### Task 2: Alt-URLs per 301 einklappen

Die drei relativen Datums-Pfade, der absolute `/am/`-Pfad und die Event-Deeplinks werden dauerhaft auf die Ortsseite umgeleitet.

**Wichtig zur Zielwahl:** `/heute`, `/morgen`, `/gestern` und `/kommendes-wochenende` leiten auf die **nackte** Ortsseite, nicht auf `?date=<berechnetes Datum>`. Ein 301 wird vom Browser dauerhaft zwischengespeichert; ein permanenter Redirect auf ein Datum, das sich täglich ändert, würde einen Nutzer für immer auf den 10. September festnageln. Nur `/am/YYYY/MM/DD` trägt ein festes Datum und darf deshalb auf `?date=` zeigen.

`checkOutdated` entfällt in diesem Task ersatzlos. Die Funktion löst nachweislich nie aus, ihre Bedingung ist invertiert, ihr Redirect-Ziel wäre die aufgerufene URL selbst gewesen, und ihre zehn Tests stehen bereits auf `it.skip` mit genau dieser Begründung im Kommentar.

**Files:**
- Modify: `packages/frontend/apps/upcoming/src/server-utils.ts`
- Modify: `packages/frontend/apps/upcoming/src/server.ts`
- Modify: `packages/frontend/apps/upcoming/src/server-utils.spec.ts`
- Delete: `packages/frontend/apps/upcoming/src/server.spec.ts`

**Interfaces:**
- Consumes: `EventsPath`, `parseEventsPath` (Task 1)
- Produces: `getLegacyRedirect(path: EventsPath): string | null`

- [ ] **Step 1: Write the failing test**

An `packages/frontend/apps/upcoming/src/server-utils.spec.ts` anhängen:

```ts
import { getLegacyRedirect } from './server-utils';

describe('getLegacyRedirect', () => {
  const redirectFor = (pathname: string) =>
    getLegacyRedirect(parseEventsPath(pathname)!);

  it('collapses the relative date paths onto the bare place page', () => {
    for (const keyword of [
      'heute',
      'morgen',
      'gestern',
      'kommendes-wochenende',
    ]) {
      expect(redirectFor(`/events/in/CH/ZG/Zug/${keyword}`)).toBe(
        '/events/in/CH/ZG/Zug',
      );
    }
  });

  it('turns an absolute date path into a date query parameter', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/09/08')).toBe(
      '/events/in/CH/ZG/Zug?date=2026-09-08',
    );
  });

  it('pads single digit months and days', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/1/5')).toBe(
      '/events/in/CH/ZG/Zug?date=2026-01-05',
    );
  });

  it('turns an event deeplink into an event query parameter', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/heute/abc-123')).toBe(
      '/events/in/CH/ZG/Zug?event=abc-123',
    );
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/09/08/abc-123')).toBe(
      '/events/in/CH/ZG/Zug?event=abc-123',
    );
  });

  it('re-encodes place names in the target', () => {
    expect(redirectFor('/events/in/CH/AG/Aarau%20Rohr/heute')).toBe(
      '/events/in/CH/AG/Aarau%20Rohr',
    );
  });

  it('returns null for urls that are already canonical', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug')).toBeNull();
    expect(redirectFor('/events/in/CH/ZG')).toBeNull();
    expect(redirectFor('/events/in/CH')).toBeNull();
  });

  it('returns null for an unrecognised trailing segment', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/irgendwas')).toBeNull();
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `getLegacyRedirect` ist nicht exportiert.

- [ ] **Step 3: Write minimal implementation**

In `server-utils.ts` ergänzen:

```ts
const RELATIVE_DATE_KEYWORDS = [
  'gestern',
  'heute',
  'morgen',
  'kommendes-wochenende',
];

const placeUrl = (path: EventsPath): string =>
  `/events/in/${encodeURIComponent(path.countryCode)}/${encodeURIComponent(
    path.region!,
  )}/${encodeURIComponent(path.place!)}`;

const pad = (value: string): string => value.padStart(2, '0');

/**
 * Liefert das 301-Ziel für eine Alt-URL, sonst null.
 *
 * Die relativen Datums-Pfade zeigen bewusst auf die nackte Ortsseite und nicht
 * auf ein berechnetes `?date=`: ein 301 wird dauerhaft zwischengespeichert, ein
 * relatives Datum ändert sich täglich. Nur `/am/` trägt ein festes Datum.
 */
export function getLegacyRedirect(path: EventsPath): string | null {
  if (!path.region || !path.place || path.rest.length === 0) {
    return null;
  }
  const base = placeUrl(path);
  const [head, ...tail] = path.rest;

  if (RELATIVE_DATE_KEYWORDS.includes(head)) {
    if (tail.length === 1) {
      return `${base}?event=${encodeURIComponent(tail[0])}`;
    }
    return tail.length === 0 ? base : null;
  }

  if (head === 'am' && tail.length >= 3) {
    const [year, month, day, ...eventId] = tail;
    if (eventId.length === 1) {
      return `${base}?event=${encodeURIComponent(eventId[0])}`;
    }
    if (eventId.length > 0) {
      return null;
    }
    return `${base}?date=${year}-${pad(month)}-${pad(day)}`;
  }

  return null;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

- [ ] **Step 5: Wire the redirect into express and remove checkOutdated**

In `server.ts` den Handler ersetzen. Der Redirect läuft **vor** der 404-Prüfung, damit `/CH/ZG/Zug/heute` umgeleitet und nicht abgewiesen wird:

```ts
app.use('/**', (req, res, next) => {
  const eventsPath = parseEventsPath(req.path);

  if (eventsPath) {
    const redirect = getLegacyRedirect(eventsPath);
    if (redirect) {
      return res.redirect(301, redirect);
    }
    if (!isKnownLocation(eventsPath)) {
      return res.status(404).send('Not found');
    }
  }

  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});
```

Den Import von `renderPath` und `upcomingBaseRoute` aus `server.ts` entfernen, falls er dadurch ungenutzt wird — `npx nx lint upcoming` meldet das.

Aus `server-utils.ts` `checkOutdated` und `OutdatedResult` ersatzlos löschen, und `packages/frontend/apps/upcoming/src/server.spec.ts` löschen: die Datei testet ausschliesslich `checkOutdated`.

- [ ] **Step 6: Run tests and lint**

Run: `cd packages/frontend && npx nx test upcoming && npx nx lint upcoming`
Expected: PASS, und die zehn übersprungenen Tests sind verschwunden (Baseline war 39 grün / 10 übersprungen).

- [ ] **Step 7: Verify manually**

```bash
for p in /events/in/CH/ZG/Zug/heute /events/in/CH/ZG/Zug/am/2026/09/08 /events/in/CH/ZG/Zug/heute/abc-123; do
  curl -s -o /dev/null -w "%{http_code} -> %{redirect_url}\n" "http://localhost:4200$p"
done
```

Expected: dreimal `301` mit den Zielen `/events/in/CH/ZG/Zug`, `…?date=2026-09-08` und `…?event=abc-123`.

- [ ] **Step 8: Commit**

```bash
git add packages/frontend/apps/upcoming/src/server-utils.ts \
        packages/frontend/apps/upcoming/src/server-utils.spec.ts \
        packages/frontend/apps/upcoming/src/server.ts
git rm packages/frontend/apps/upcoming/src/server.spec.ts
git commit -m "feat(frontend): redirect legacy date urls onto the place page"
```

---

### Task 3: Datum und Event-Deeplink auf Query-Parameter umstellen

Der Kern des Umbaus. Die Routen verlieren ihre Datums-Zweige, die Ortsroute bekommt den bestehenden `eventsResolver`, und das Datum kommt aus `?date=`.

**Files:**
- Modify: `packages/frontend/apps/upcoming/src/app/upcoming-product-routes.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/app.routes.server.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/upcoming-product-routes.spec.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/events-resolver.spec.ts`

**Interfaces:**
- Produces:
  - `parseDateFromQuery(queryParams: Params): { date: Dayjs; explicit: boolean }` in `upcoming-product-routes.ts`
  - `renderPlaceUrl(countryCode: string, region: string, place: string): string` in `upcoming-product-routes.ts`
- Removes: `parseDateFromUrl`, `parseRelativeDate`, `relativeDateParser`, `relativeDateIncrement`, `RelativeDate`, `renderUrlWithAbsoluteDate`, `renderUrlWithRelativeDate`

- [ ] **Step 1: Write the failing test**

In `packages/frontend/apps/upcoming/src/app/upcoming-product-routes.spec.ts` ergänzen:

```ts
import dayjs from 'dayjs';
import { parseDateFromQuery, renderPlaceUrl } from './upcoming-product-routes';

describe('parseDateFromQuery', () => {
  it('falls back to today when no date is given', () => {
    const { date, explicit } = parseDateFromQuery({});
    expect(date.format('YYYY-MM-DD')).toBe(dayjs().format('YYYY-MM-DD'));
    expect(explicit).toBe(false);
  });

  it('reads an ISO date', () => {
    const { date, explicit } = parseDateFromQuery({ date: '2026-09-12' });
    expect(date.format('YYYY-MM-DD')).toBe('2026-09-12');
    expect(explicit).toBe(true);
  });

  it('falls back to today for an unparsable date', () => {
    const { date, explicit } = parseDateFromQuery({ date: 'gestern' });
    expect(date.format('YYYY-MM-DD')).toBe(dayjs().format('YYYY-MM-DD'));
    expect(explicit).toBe(false);
  });
});

describe('renderPlaceUrl', () => {
  it('renders and encodes the place url', () => {
    expect(renderPlaceUrl('CH', 'ZG', 'Zug')).toBe('/events/in/CH/ZG/Zug');
    expect(renderPlaceUrl('CH', 'AG', 'Aarau Rohr')).toBe(
      '/events/in/CH/AG/Aarau%20Rohr',
    );
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — beide Funktionen existieren nicht.

- [ ] **Step 3: Rewrite the route tree**

In `upcoming-product-routes.ts` den `events`-Zweig auf drei Ebenen kürzen:

```ts
  events: {
    path: ['events', 'in'],
    children: {
      countryCode: {
        path: [upperCaseStringParser('countryCode')],
        children: {
          region: {
            path: [str('region')],
            children: {
              place: {
                path: [str('place')],
              },
            },
          },
        },
      },
    },
  },
```

`parseDateFromUrl`, `parseRelativeDate`, `relativeDateParser`, `relativeDateIncrement` und `RelativeDate` ersatzlos löschen, und stattdessen ergänzen:

```ts
export function parseDateFromQuery(queryParams: Params): {
  date: Dayjs;
  explicit: boolean;
} {
  const raw = queryParams['date'];
  if (typeof raw === 'string') {
    const parsed = dayjs(raw, 'YYYY-MM-DD');
    if (parsed.isValid() && /^\d{4}-\d{2}-\d{2}$/.test(raw)) {
      return { date: parsed, explicit: true };
    }
  }
  return { date: dayjs(), explicit: false };
}

export function renderPlaceUrl(
  countryCode: string,
  region: string,
  place: string,
): string {
  return renderPath(upcomingBaseRoute.events.countryCode.region.place, {
    countryCode,
    region,
    place,
  });
}
```

`renderPath` aus `typesafe-routes` importieren.

- [ ] **Step 4: Move the resolver onto the place route**

In `UPCOMING_ROUTES` die vier Einträge für `dateTime`, `relativeDateTime` und die beiden `eventId`-Varianten löschen. Der `place`-Eintrag bekommt den Resolver:

```ts
  {
    resolve: {
      events: eventsResolver,
    },
    path: template(upcomingBaseRoute._.events.countryCode.region.place),
    loadComponent: () =>
      import('./pages/event-calendar/event-calendar.page').then(
        (m) => m.EventCalendarPage,
      ),
  },
```

Im `eventsResolver` die Datumsquelle umstellen — `route.params` wird zu `route.queryParams`:

```ts
  const { date } = parseDateFromQuery(route.queryParams);
```

In `app.routes.server.ts` die beiden Einträge für `dateTime` und `relativeDateTime` löschen. Die drei verbleibenden `events`-Einträge bleiben auf `RenderMode.Server`.

- [ ] **Step 5: Update the page component**

In `event-calendar.page.ts`:

`ngOnInit` liest im Browser-Zweig künftig Query-Parameter statt Pfad-Parameter. `this.activatedRoute.params.subscribe` wird zu einer Kombination aus beidem — der Ort steht im Pfad, das Datum in der Query:

```ts
      this.subscriptions.push(
        combineLatest([
          this.activatedRoute.params,
          this.activatedRoute.queryParams,
        ]).subscribe(async ([, queryParams]) => {
          try {
            this.namedLatLon = await parseLocationFromUrl(
              this.activatedRoute.snapshot,
              this.adminGeoService,
            );
            this.saveLocation(this.namedLatLon);
            this.perimeter = 10;

            const { date } = parseDateFromQuery(queryParams);
            await this.changeDate(date);

            this.changeRef.detectChanges();
            this.pageService.setMetaTags(this.getPageTags());

            const eventId = queryParams['event'];
            if (eventId != null) {
              await this.openEventModalForId(String(eventId));
            }
          } catch (e) {
            if (this.headerComponent()) {
              await this.headerComponent().fetchSuggestions('');
            }
          } finally {
            this.loading = false;
          }
          this.changeRef.detectChanges();
        }),
      );
```

`combineLatest` aus `rxjs` importieren. Das Feld `dateIsFromRelativeUrl` und seine beiden Zuweisungen entfallen.

`createDateUrl` verliert die Relativdatums-Verzweigung vollständig:

```ts
  createDateUrl(
    date: Nullable<Dayjs>,
    location: Nullable<NamedLatLon> = null,
  ): string {
    const { countryCode, region, place } = this.getLocationOrElse(location);
    const base = renderPlaceUrl(countryCode, region, place);
    if (!date || date.isSame(dayjs(), 'day')) {
      return base;
    }
    return `${base}?date=${date.format('YYYY-MM-DD')}`;
  }
```

`createEventUrl` und `getPlaceUrl` folgen demselben Muster:

```ts
  createEventUrl(event: LocalizedEvent): string {
    const { countryCode, region, place } = this.getLocationOrElse(
      this.namedLatLon,
    );
    const base = renderPlaceUrl(countryCode, region, place);
    return `${base}?event=${encodeURIComponent(String((event as any).id))}`;
  }

  getPlaceUrl(location: NamedLatLon): string {
    if (!location) {
      return '';
    }
    return this.createDateUrl(this.date, location);
  }
```

`getDateOrElse` wird dadurch ungenutzt und entfällt, ebenso die beiden Export-Funktionen `renderUrlWithAbsoluteDate` und `renderUrlWithRelativeDate` am Dateiende.

- [ ] **Step 6: Point the canonical at the bare place url**

Ebenfalls in `event-calendar.page.ts`. `getCanonicalUrlFromPath` schneidet den Query-String ab, und `getPageTags` setzt `noindex`, sobald einer gesetzt ist:

```ts
  private getCanonicalUrlFromPath(): string {
    const path = this.locationService.path();
    if (!path) {
      return 'https://lokale.events/';
    }
    return `https://lokale.events${path.split('?')[0]}`;
  }
```

```ts
    const queryParams = this.activatedRoute.snapshot.queryParams;
    const robots =
      queryParams['event'] || queryParams['date']
        ? 'noindex, follow'
        : 'index, follow';
```

Der Titel verliert seinen Relativdatums-Teil und benennt stattdessen den Ort:

```ts
      return {
        title: `Events in ${location.displayName}, ${location.area} | lokale.events`,
        …
```

- [ ] **Step 7: Update the resolver spec**

In `events-resolver.spec.ts` das `route`-Objekt anpassen: `relativeDate: 'heute'` entfällt aus `params`, dafür kommt `queryParams: {}` dazu:

```ts
  const route = {
    params: {
      countryCode: 'CH',
      region: 'ZH',
      place: 'Hedingen',
    },
    queryParams: {},
  } as unknown as ActivatedRouteSnapshot;
```

Der Kommentarblock am Dateikopf nennt die drei Relativdatums-URLs; ihn auf die Ortsseite umschreiben, ohne die Aussage zu verändern — der Resolver muss weiterhin degradieren statt abzulehnen.

- [ ] **Step 8: Run tests and lint**

Run: `cd packages/frontend && npx nx test upcoming && npx nx lint upcoming`
Expected: PASS. Der Lint meldet zuverlässig, welche Importe durch die Löschungen verwaist sind.

- [ ] **Step 9: Verify manually**

```bash
curl -s http://localhost:4200/events/in/CH/ZG/Zug | grep -o '<h1[^>]*>[^<]*</h1>'
curl -s "http://localhost:4200/events/in/CH/ZG/Zug?date=2026-09-12" | grep -o '<link rel="canonical"[^>]*>\|<meta name="robots"[^>]*>'
```

Expected: die Ortsseite rendert ein `<h1>`, und bei gesetztem `?date=` zeigt der Canonical auf `https://lokale.events/events/in/CH/ZG/Zug` bei `robots: noindex, follow`.

- [ ] **Step 10: Commit**

```bash
git add packages/frontend/apps/upcoming/src/app
git commit -m "feat(frontend): move date and event deeplink into query parameters"
```

---

### Task 4: Hub-Seiten für Land und Kanton

Beide Ebenen rendern heute eine leere Seite. Sie bekommen eigene Komponenten statt `EventCalendarPage` — die ist mit 997 Zeilen am Limit, und die Hubs brauchen weder Resolver noch GraphQL. Datenquelle ist ausschliesslich `getCachedLocations()`.

**Files:**
- Create: `packages/frontend/apps/upcoming/src/app/pages/country-hub/country-hub.page.ts`
- Create: `packages/frontend/apps/upcoming/src/app/pages/country-hub/country-hub.page.html`
- Create: `packages/frontend/apps/upcoming/src/app/pages/country-hub/country-hub.page.spec.ts`
- Create: `packages/frontend/apps/upcoming/src/app/pages/region-hub/region-hub.page.ts`
- Create: `packages/frontend/apps/upcoming/src/app/pages/region-hub/region-hub.page.html`
- Create: `packages/frontend/apps/upcoming/src/app/pages/region-hub/region-hub.page.spec.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/upcoming-product-routes.ts`

**Interfaces:**
- Consumes: `renderPlaceUrl` (Task 3)
- Produces: `CountryHubPage`, `RegionHubPage`; beide exportieren `regions`/`places` als lesbare Getter für den Test

- [ ] **Step 1: Write the failing test**

`packages/frontend/apps/upcoming/src/app/pages/country-hub/country-hub.page.spec.ts`:

```ts
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CountryHubPage } from './country-hub.page';

describe('CountryHubPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CountryHubPage],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('lists every canton with its place count', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const regions = fixture.componentInstance.regions;

    expect(regions.length).toBeGreaterThan(0);
    expect(regions.map((r) => r.area)).toContain('ZG');
    expect(regions.every((r) => r.placeCount > 0)).toBe(true);
    expect(regions.map((r) => r.area)).toEqual(
      [...regions.map((r) => r.area)].sort(),
    );
  });

  it('renders one link per canton and an h1', () => {
    const fixture = TestBed.createComponent(CountryHubPage);
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain('Schweiz');
    expect(element.querySelectorAll('a[href^="/events/in/CH/"]').length).toBe(
      fixture.componentInstance.regions.length,
    );
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `CountryHubPage` existiert nicht.

- [ ] **Step 3: Implement the country hub**

`country-hub.page.ts`:

```ts
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import dayjs from 'dayjs';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { PageService, PageTags } from '@feedless/components';
import { getCachedLocations } from '@feedless/geo';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';

export type RegionSummary = {
  area: string;
  placeCount: number;
  url: string;
};

@Component({
  selector: 'app-country-hub-page',
  templateUrl: './country-hub.page.html',
  imports: [IonContent, RouterLink, UpcomingFooterComponent],
  standalone: true,
})
export class CountryHubPage implements OnInit {
  private readonly pageService = inject(PageService);

  readonly regions: RegionSummary[] = this.collectRegions();

  ngOnInit(): void {
    this.pageService.setMetaTags(this.getPageTags());
  }

  private collectRegions(): RegionSummary[] {
    const byArea = new Map<string, Set<string>>();
    for (const location of getCachedLocations()) {
      if (!byArea.has(location.area)) {
        byArea.set(location.area, new Set());
      }
      byArea.get(location.area)!.add(location.place);
    }
    return [...byArea.entries()]
      .map(([area, places]) => ({
        area,
        placeCount: places.size,
        url: `/events/in/CH/${encodeURIComponent(area)}`,
      }))
      .sort((a, b) => a.area.localeCompare(b.area));
  }

  private getPageTags(): PageTags {
    return {
      title: 'Veranstaltungen in der Schweiz | lokale.events',
      description: `Lokale Veranstaltungen in ${this.regions.length} Kantonen. Wähle deinen Kanton und finde Events in deiner Nähe.`,
      publisher: 'lokale.events',
      category: 'Events',
      url: 'https://lokale.events/events/in/CH',
      canonicalUrl: 'https://lokale.events/events/in/CH',
      lang: 'de',
      publishedAt: dayjs(),
      author: 'lokale.events Team',
      robots: 'index, follow',
    };
  }
}
```

`country-hub.page.html`:

```html
<ion-content>
  <main id="main-content" class="events">
    <h1>Veranstaltungen in der Schweiz</h1>
    <p>
      Lokale Veranstaltungen aus {{ regions.length }} Kantonen. Wähle einen
      Kanton, um alle Orte zu sehen.
    </p>
    <ul>
      @for (region of regions; track region.area) {
        <li>
          <a [routerLink]="region.url">
            Veranstaltungen im Kanton {{ region.area }}
          </a>
          <span>{{ region.placeCount }} Orte</span>
        </li>
      }
    </ul>
  </main>
  <app-upcoming-footer></app-upcoming-footer>
</ion-content>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

- [ ] **Step 5: Write the failing test for the region hub**

`packages/frontend/apps/upcoming/src/app/pages/region-hub/region-hub.page.spec.ts`:

```ts
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { RegionHubPage } from './region-hub.page';

describe('RegionHubPage', () => {
  const createFixture = (region: string) => {
    TestBed.configureTestingModule({
      imports: [RegionHubPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { params: { countryCode: 'CH', region } } },
        },
      ],
    });
    const fixture = TestBed.createComponent(RegionHubPage);
    fixture.detectChanges();
    return fixture;
  };

  afterEach(() => TestBed.resetTestingModule());

  it('lists the places of the canton, alphabetically', () => {
    const fixture = createFixture('ZG');
    const places = fixture.componentInstance.places;

    expect(places.length).toBeGreaterThan(0);
    expect(places.map((p) => p.place)).toContain('Zug');
    expect(places.map((p) => p.place)).toEqual(
      [...places.map((p) => p.place)].sort(),
    );
  });

  it('links every place to its place page and renders an h1', () => {
    const fixture = createFixture('ZG');
    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('h1')?.textContent).toContain('ZG');
    expect(
      element.querySelectorAll('a[href^="/events/in/CH/ZG/"]').length,
    ).toBe(fixture.componentInstance.places.length);
  });

  it('yields no places for an unknown canton', () => {
    expect(createFixture('QQ').componentInstance.places).toEqual([]);
  });
});
```

- [ ] **Step 6: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `RegionHubPage` existiert nicht.

- [ ] **Step 7: Implement the region hub**

`region-hub.page.ts`:

```ts
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import dayjs from 'dayjs';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { PageService, PageTags } from '@feedless/components';
import { getCachedLocations } from '@feedless/geo';
import { renderPlaceUrl } from '../../upcoming-product-routes';
import { UpcomingFooterComponent } from '../../components/upcoming-footer/upcoming-footer.component';

export type PlaceSummary = {
  place: string;
  url: string;
};

@Component({
  selector: 'app-region-hub-page',
  templateUrl: './region-hub.page.html',
  imports: [IonContent, RouterLink, UpcomingFooterComponent],
  standalone: true,
})
export class RegionHubPage implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly pageService = inject(PageService);

  readonly region: string =
    this.activatedRoute.snapshot.params['region'] ?? '';
  readonly places: PlaceSummary[] = this.collectPlaces();

  ngOnInit(): void {
    this.pageService.setMetaTags(this.getPageTags());
  }

  private collectPlaces(): PlaceSummary[] {
    const names = new Set(
      getCachedLocations()
        .filter(
          (location) =>
            location.area.toLowerCase() === this.region.toLowerCase(),
        )
        .map((location) => location.place),
    );
    return [...names]
      .sort((a, b) => a.localeCompare(b))
      .map((place) => ({
        place,
        url: renderPlaceUrl('CH', this.region, place),
      }));
  }

  private getPageTags(): PageTags {
    const url = `https://lokale.events/events/in/CH/${encodeURIComponent(this.region)}`;
    return {
      title: `Veranstaltungen im Kanton ${this.region} | lokale.events`,
      description: `Lokale Veranstaltungen in ${this.places.length} Orten im Kanton ${this.region}. Wähle deinen Ort und finde Events in deiner Nähe.`,
      publisher: 'lokale.events',
      category: 'Events',
      url,
      canonicalUrl: url,
      region: this.region,
      lang: 'de',
      publishedAt: dayjs(),
      author: 'lokale.events Team',
      robots: 'index, follow',
    };
  }
}
```

`region-hub.page.html`:

```html
<ion-content>
  <main id="main-content" class="events">
    <h1>Veranstaltungen im Kanton {{ region }}</h1>
    <p>
      {{ places.length }} Orte im Kanton {{ region }}. Wähle einen Ort, um
      seine Veranstaltungen zu sehen.
    </p>
    <ul>
      @for (place of places; track place.place) {
        <li>
          <a [routerLink]="place.url">
            Veranstaltungen in {{ place.place }}
          </a>
        </li>
      }
    </ul>
  </main>
  <app-upcoming-footer></app-upcoming-footer>
</ion-content>
```

- [ ] **Step 8: Add BreadcrumbList and ItemList JSON-LD to both hubs**

Die Spec verlangt für beide Hub-Ebenen strukturierte Daten. `PageService.setJsonLdData` wird bereits von `EventCalendarPage` benutzt und nimmt ein `WebPage`-Objekt aus `schema-dts`.

In `country-hub.page.ts` in `ngOnInit` ergänzen:

```ts
    this.pageService.setJsonLdData(this.createSchema());
```

```ts
  private createSchema(): WebPage {
    const url = 'https://lokale.events/events/in/CH';
    return {
      '@type': 'WebPage',
      name: 'Veranstaltungen in der Schweiz',
      url,
      inLanguage: 'de-CH',
      breadcrumb: {
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            item: { '@id': url, name: 'Veranstaltungen in der Schweiz' },
          },
        ],
      },
      mainEntity: {
        '@type': 'ItemList',
        name: 'Kantone',
        itemListElement: this.regions.map((region, index) => ({
          '@type': 'ListItem',
          position: index + 1,
          item: {
            '@id': `https://lokale.events${region.url}`,
            name: `Veranstaltungen im Kanton ${region.area}`,
          },
        })),
      },
    };
  }
```

`import { WebPage } from 'schema-dts';` ergänzen.

In `region-hub.page.ts` dasselbe Muster, mit zwei Breadcrumb-Stufen (Land, dann Kanton) und den Orten als `ItemList`:

```ts
  private createSchema(): WebPage {
    const countryUrl = 'https://lokale.events/events/in/CH';
    const url = `${countryUrl}/${encodeURIComponent(this.region)}`;
    return {
      '@type': 'WebPage',
      name: `Veranstaltungen im Kanton ${this.region}`,
      url,
      inLanguage: 'de-CH',
      breadcrumb: {
        '@type': 'BreadcrumbList',
        itemListElement: [
          {
            '@type': 'ListItem',
            position: 1,
            item: { '@id': countryUrl, name: 'Veranstaltungen in der Schweiz' },
          },
          {
            '@type': 'ListItem',
            position: 2,
            item: { '@id': url, name: `Veranstaltungen im Kanton ${this.region}` },
          },
        ],
      },
      mainEntity: {
        '@type': 'ItemList',
        name: `Orte im Kanton ${this.region}`,
        itemListElement: this.places.map((place, index) => ({
          '@type': 'ListItem',
          position: index + 1,
          item: {
            '@id': `https://lokale.events${place.url}`,
            name: `Veranstaltungen in ${place.place}`,
          },
        })),
      },
    };
  }
```

In beiden Specs je einen Test ergänzen, der prüft, dass `setJsonLdData` mit einer `ItemList` aufgerufen wurde, deren Länge der Zahl der gerenderten Links entspricht — `PageService` dafür als Spy-Objekt in `providers` mitgeben.

- [ ] **Step 9: Point the routes at the new components**

In `upcoming-product-routes.ts` die beiden Hub-Einträge umhängen:

```ts
  {
    path: template(upcomingBaseRoute._.events.countryCode),
    loadComponent: () =>
      import('./pages/country-hub/country-hub.page').then(
        (m) => m.CountryHubPage,
      ),
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode.region),
    loadComponent: () =>
      import('./pages/region-hub/region-hub.page').then((m) => m.RegionHubPage),
  },
```

- [ ] **Step 10: Run tests and verify manually**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

```bash
curl -s http://localhost:4200/events/in/CH | grep -c 'href="/events/in/CH/'
curl -s http://localhost:4200/events/in/CH/ZG | grep -c 'href="/events/in/CH/ZG/'
```

Expected: jeweils eine Zahl grösser null — heute liefern beide `0`.

- [ ] **Step 11: Commit**

```bash
git add packages/frontend/apps/upcoming/src/app
git commit -m "feat(frontend): render country and canton hub pages"
```

---

### Task 5: Sitemap auf Hubs und Ortsseiten umstellen

Der Generator schreibt heute 2250 Datums-URLs und keine einzige Hub-URL. Künftig sind es 1 Land + 26 Kantone + rund 749 Orte.

**Files:**
- Create: `packages/frontend/apps/upcoming/src/sitemap-links.ts`
- Create: `packages/frontend/apps/upcoming/src/sitemap-links.spec.ts`
- Modify: `packages/frontend/apps/upcoming/generate-sitemaps.ts`

**Interfaces:**
- Produces:
  - `type SitemapPlace = { place: string; area: string }`
  - `type SitemapLink = { url: string; changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly'; lastmod: string; priority: number }`
  - `buildSitemapLinks(places: SitemapPlace[], lastMod: string): SitemapLink[]`

Die Linkliste zieht in eine eigene Datei unter `src/`, und die Ortsliste kommt als Parameter herein statt als Import. Zwei Gründe: `generate-sitemaps.ts` führt beim Import sofort `new AppsDataGenerator(...)` aus und schriebe beim Testlauf eine Datei, und der Generator läuft als reines Node-Skript ausserhalb der Angular-Auflösung — eine Funktion ohne Importe ist in beiden Welten gleich benutzbar.

- [ ] **Step 1: Write the failing test**

`packages/frontend/apps/upcoming/src/sitemap-links.spec.ts`:

```ts
import { buildSitemapLinks, SitemapPlace } from './sitemap-links';

describe('buildSitemapLinks', () => {
  const places: SitemapPlace[] = [
    { place: 'Zug', area: 'ZG' },
    { place: 'Baar', area: 'ZG' },
    { place: 'Aarau Rohr', area: 'AG' },
  ];
  const links = buildSitemapLinks(places, '2026-09-09T00:00:00.000Z');
  const urls = links.map((link) => link.url);

  it('contains the static pages', () => {
    expect(urls).toContain('/');
    expect(urls).toContain('/ueber-uns/');
    expect(urls).toContain('/agb/');
  });

  it('contains the country hub and one hub per canton', () => {
    expect(urls).toContain('/events/in/CH');
    expect(urls).toContain('/events/in/CH/ZG');
    expect(urls).toContain('/events/in/CH/AG');
  });

  it('contains one bare url per place, percent-encoded', () => {
    expect(urls).toContain('/events/in/CH/ZG/Zug');
    expect(urls).toContain('/events/in/CH/AG/Aarau%20Rohr');
  });

  it('contains no date urls any more', () => {
    expect(
      urls.filter((url) =>
        /\/(heute|morgen|gestern|kommendes-wochenende)$/.test(url),
      ),
    ).toEqual([]);
    expect(urls.filter((url) => url.includes('/am/'))).toEqual([]);
  });

  it('lists every url exactly once', () => {
    expect(new Set(urls).size).toBe(urls.length);
  });

  it('carries the given lastmod on every entry', () => {
    expect(
      links.every((link) => link.lastmod === '2026-09-09T00:00:00.000Z'),
    ).toBe(true);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `./sitemap-links` existiert nicht.

- [ ] **Step 3: Write the pure link builder**

`packages/frontend/apps/upcoming/src/sitemap-links.ts`:

```ts
export type SitemapPlace = {
  place: string;
  area: string;
};

export type SitemapLink = {
  url: string;
  changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly';
  lastmod: string;
  priority: number;
};

export function buildSitemapLinks(
  places: SitemapPlace[],
  lastMod: string,
): SitemapLink[] {
  const links: SitemapLink[] = [
    { url: '/', changefreq: 'daily', lastmod: lastMod, priority: 1.0 },
    {
      url: '/ueber-uns/',
      changefreq: 'monthly',
      lastmod: lastMod,
      priority: 0.8,
    },
    { url: '/agb/', changefreq: 'yearly', lastmod: lastMod, priority: 0.3 },
    {
      url: '/events/in/CH',
      changefreq: 'weekly',
      lastmod: lastMod,
      priority: 0.9,
    },
  ];

  const regions = [
    ...new Set(places.map((place) => place.area.toUpperCase())),
  ].sort();
  for (const region of regions) {
    links.push({
      url: `/events/in/CH/${region}`,
      changefreq: 'weekly',
      lastmod: lastMod,
      priority: 0.8,
    });
  }

  for (const location of places) {
    links.push({
      url: `/events/in/CH/${location.area.toUpperCase()}/${encodeURIComponent(location.place)}`,
      changefreq: 'daily',
      lastmod: lastMod,
      priority: 0.9,
    });
  }

  return links;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

- [ ] **Step 5: Use it from the generator**

In `generate-sitemaps.ts` die Methode `generateUpcomingSitemapLinks` ersatzlos löschen und den Aufruf in `generateSiteMap` umstellen:

```ts
import { buildSitemapLinks } from './src/sitemap-links.ts';
```

```ts
    const links = buildSitemapLinks(places, lastMod);
```

Der bestehende `places`-Aufbau am Dateikopf bleibt unverändert — er dedupliziert bereits über `${area}/${place}`, sortiert, und liefert genau `{ place, area }`.

Zusätzlich die letzte Zeile der Datei absichern, damit ein Import die Generierung nicht auslöst:

```ts
if (process.argv[1]?.endsWith('generate-sitemaps.ts')) {
  new AppsDataGenerator(join(process.cwd(), 'apps/upcoming/public'));
}
```

- [ ] **Step 6: Regenerate and inspect the sitemap**

Run: `cd packages/frontend && npm run generate:sitemap`

```bash
grep -c "<url>" apps/upcoming/public/sitemap.xml
grep -c "heute\|morgen\|wochenende" apps/upcoming/public/sitemap.xml
grep -c "<loc>https://lokale.events/events/in/CH/[A-Z][A-Z]</loc>" apps/upcoming/public/sitemap.xml
```

Expected: rund 776 statt 2250 URLs, **0** Treffer für die Datums-Schlüsselwörter, und eine Kantonszahl grösser null.

- [ ] **Step 7: Commit**

```bash
git add packages/frontend/apps/upcoming/generate-sitemaps.ts \
        packages/frontend/apps/upcoming/src/sitemap-links.ts \
        packages/frontend/apps/upcoming/src/sitemap-links.spec.ts \
        packages/frontend/apps/upcoming/public/sitemap.xml
git commit -m "feat(frontend): list hub and place urls in the sitemap"
```

---

### Task 6: robots.txt aufräumen

Die `Disallow`-Regeln zeigen auf URLs, die es nach Task 2 nicht mehr gibt. Gesteuert wird künftig über Canonical und `noindex`, nicht über Crawl-Blockade — nur so kann Google die Konsolidierungssignale überhaupt lesen.

**Files:**
- Modify: `packages/frontend/apps/upcoming/public/robots.txt`

- [ ] **Step 1: Rewrite the file**

```
User-agent: *
Allow: /

# Private areas
Disallow: /management/
Disallow: /login
Disallow: /profile

Sitemap: https://lokale.events/sitemap.xml
```

- [ ] **Step 2: Verify**

```bash
grep -c "heute\|morgen\|wochenende\|/am/" packages/frontend/apps/upcoming/public/robots.txt
```

Expected: `0`.

- [ ] **Step 3: Commit**

```bash
git add packages/frontend/apps/upcoming/public/robots.txt
git commit -m "chore(frontend): drop robots rules for removed date urls"
```

---

### Task 7: Cache-Control für gerenderte Seiten

Heute trägt keine Antwort `Cache-Control`, `ETag` oder `Last-Modified`. Bei 776 URLs, deren Inhalt sich genau einmal pro Tag ändert, hängt jeder Crawl am SSR-Rendering.

**Abweichung von der Spec, bewusst:** Die Spec verlangt „ein Test, dass gerenderte Seiten einen `Cache-Control`-Header tragen". Dieses Projekt hat keinen Express-Integrationstest und keine Test-HTTP-Bibliothek; einen solchen Test einzuführen hiesse, eine Abhängigkeit und ein Test-Setup aufzubauen, das über diesen Block hinausgeht. Stattdessen ist die reine Funktion unit-getestet und die Verdrahtung wird per curl gegen den Dev-Server geprüft. Dasselbe gilt für die 301- und 404-Verdrahtung aus Task 1 und 2: die Entscheidungslogik ist vollständig getestet, die Express-Anbindung nicht. Wenn dir das zu wenig ist, ist ein eigener Vorgang „Express-Integrationstests mit supertest" der richtige Ort dafür.

**Files:**
- Modify: `packages/frontend/apps/upcoming/src/server-utils.ts`
- Modify: `packages/frontend/apps/upcoming/src/server.ts`
- Modify: `packages/frontend/apps/upcoming/src/server-utils.spec.ts`

**Interfaces:**
- Produces: `secondsUntilMidnight(now: Date): number`

- [ ] **Step 1: Write the failing test**

An `server-utils.spec.ts` anhängen:

```ts
import { secondsUntilMidnight } from './server-utils';

describe('secondsUntilMidnight', () => {
  it('counts the seconds left in the day', () => {
    expect(secondsUntilMidnight(new Date('2026-09-09T23:59:00Z'))).toBe(60);
    expect(secondsUntilMidnight(new Date('2026-09-09T00:00:00Z'))).toBe(86400);
  });

  it('never returns zero, so the header is always usable', () => {
    expect(
      secondsUntilMidnight(new Date('2026-09-09T23:59:59.999Z')),
    ).toBeGreaterThan(0);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL — `secondsUntilMidnight` existiert nicht.

- [ ] **Step 3: Write minimal implementation**

In `server-utils.ts`:

```ts
/** Sekunden bis zum nächsten UTC-Mitternacht, mindestens eine. */
export function secondsUntilMidnight(now: Date): number {
  const midnight = Date.UTC(
    now.getUTCFullYear(),
    now.getUTCMonth(),
    now.getUTCDate() + 1,
  );
  return Math.max(1, Math.round((midnight - now.getTime()) / 1000));
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

- [ ] **Step 5: Set the header in express**

In `server.ts`, direkt vor `angularApp.handle(req)`:

```ts
  const maxAge = secondsUntilMidnight(new Date());
  res.setHeader(
    'Cache-Control',
    `public, max-age=0, s-maxage=${maxAge}, stale-while-revalidate=3600`,
  );
```

`max-age=0` hält den Browser-Cache aussen vor, `s-maxage` gilt für CDN und Reverse Proxy, `stale-while-revalidate` verhindert, dass um Mitternacht alle Kanten gleichzeitig auf den SSR durchschlagen.

- [ ] **Step 6: Verify manually**

```bash
curl -sI http://localhost:4200/events/in/CH/ZG/Zug | grep -i cache-control
```

Expected: eine `cache-control`-Zeile mit `s-maxage`.

- [ ] **Step 7: Commit**

```bash
git add packages/frontend/apps/upcoming/src/server-utils.ts \
        packages/frontend/apps/upcoming/src/server-utils.spec.ts \
        packages/frontend/apps/upcoming/src/server.ts
git commit -m "perf(frontend): cache rendered pages until midnight"
```

---

### Task 8: Locale korrigieren

`og:locale` steht fest auf `de_DE`, korrekt wäre `de_CH`. Der Wert ist in `libs/components` verdrahtet und wird von allen Nx-Apps geteilt, deshalb wird er über `PageTags` konfigurierbar — mit `de_DE` als Vorgabe, damit die anderen Apps unverändert bleiben.

**Entscheidung:** `og:image` ist aus diesem Task gestrichen. `PageTags.image` existiert bereits und wird von `setOpenGraphTags` und `setTwitterCardTags` ausgewertet — sobald ein Bild vorliegt, ist es eine Einzeile pro Seite. Geteilte Links bleiben bis dahin ohne Vorschaubild; das bleibt Befund 5 der Spec und ist unerledigt.

**Files:**
- Modify: `packages/frontend/libs/components/src/lib/services/page.service.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/country-hub/country-hub.page.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/region-hub/region-hub.page.ts`

- [ ] **Step 1: Make the locale configurable**

In `page.service.ts` das Feld ergänzen:

```ts
export type PageTags = {
  …
  locale?: string;
};
```

und in `setOpenGraphTags` verwenden:

```ts
      { property: 'og:locale', content: options.locale ?? 'de_DE' },
```

Die Vorgabe `de_DE` ist wichtig: `PageService` wird von allen Apps im Nx-Workspace benutzt, und dieser Task ändert nur `upcoming`.

- [ ] **Step 2: Set the Swiss locale on all three upcoming pages**

In `getPageTags` von `event-calendar.page.ts`, `country-hub.page.ts` und `region-hub.page.ts` jeweils ergänzen:

```ts
      lang: 'de-CH',
      locale: 'de_CH',
```

In `event-calendar.page.ts` gilt das für **beide** Rückgabezweige von `getPageTags`, den mit und den ohne `location`.

- [ ] **Step 3: Run tests and verify manually**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

```bash
curl -s http://localhost:4200/events/in/CH/ZG/Zug | grep -o '<meta property="og:locale"[^>]*>'
curl -s http://localhost:4200/events/in/CH/ZG/Zug | grep -o '<html[^>]*lang="[^"]*"'
```

Expected: `og:locale` auf `de_CH`, `lang="de-CH"`.

- [ ] **Step 4: Commit**

```bash
git add packages/frontend/libs/components/src/lib/services/page.service.ts \
        packages/frontend/apps/upcoming/src/app
git commit -m "feat(frontend): use the swiss locale on upcoming pages"
```

---

### Task 9: Template aufräumen — h1 zuerst, kein Bot-Text, sichtbare Uhrzeit

Drei Befunde aus der Spec in einer Datei: die Gliederung beginnt heute bei `h3`, 33 Elemente tragen `display: none` mit `itemprop`-Inhalten, und die Startzeit steht ausschliesslich in einem versteckten `<meta>`.

**Files:**
- Modify: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.html`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.scss`
- Modify: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.ts`
- Modify: `packages/frontend/apps/upcoming/src/app/components/inline-calendar/inline-calendar.component.html`
- Test: `packages/frontend/apps/upcoming/src/app/pages/event-calendar/event-calendar.page.spec.ts`

- [ ] **Step 1: Write the failing test**

An `event-calendar.page.spec.ts` anhängen — die bestehende `beforeEach`-Konfiguration der Datei weiterverwenden:

```ts
  it('renders the h1 before any deeper heading', () => {
    const fixture = TestBed.createComponent(EventCalendarPage);
    fixture.detectChanges();
    const headings = Array.from(
      (fixture.nativeElement as HTMLElement).querySelectorAll(
        'h1, h2, h3, h4, h5, h6',
      ),
    ).map((element) => element.tagName.toLowerCase());

    if (headings.length > 0) {
      expect(headings[0]).toBe('h1');
    }
  });

  it('renders no bot-only elements', () => {
    const fixture = TestBed.createComponent(EventCalendarPage);
    fixture.detectChanges();
    expect(
      (fixture.nativeElement as HTMLElement).querySelectorAll('.bot-only')
        .length,
    ).toBe(0);
  });
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: FAIL beim `bot-only`-Test, sobald die Seite Events rendert; der Reihenfolge-Test schlägt fehl, sobald der Inline-Kalender `h3` liefert.

- [ ] **Step 3: Demote the calendar headings**

Im `inline-calendar.component.html` sind die Tagesbeschriftungen Navigation, keine Gliederung. Jedes `<h3>` dort durch ein `<span>` ersetzen und die zugehörigen Regeln in `inline-calendar.component.scss` von `h3` auf `span` umstellen. Der Kalender bleibt optisch unverändert.

- [ ] **Step 4: Remove the bot-only markup**

Aus `event-calendar.page.html` sämtliche Elemente mit `class="bot-only"` ersatzlos löschen — das sind die `<meta itemprop="startDate">`, `<meta itemprop="description">`, die `<div itemprop="location">`-Blöcke und die versteckten `<span itemprop="description">` in den Ortsüberschriften. Dieselben Daten stehen vollständig im JSON-LD aus `createWebsiteSchema`, das unverändert bleibt.

Aus `event-calendar.page.scss` die Regel `.bot-only { display: none; }` löschen.

- [ ] **Step 5: Render the start time**

In `event-calendar.page.html` innerhalb des `<article>` unter der Überschrift die Uhrzeit ergänzen:

```html
                                        <header>
                                          <h3 itemprop="name"> {{ cleanTitle(event.title) }} </h3>
                                        </header>
                                        @if (formatTime(event.startingAt); as time) {
                                          <div class="event-details">
                                            <time [attr.datetime]="toIsoString(event.startingAt)">{{ time }} Uhr</time>
                                          </div>
                                        }
```

Die Klasse `.event-details` existiert bereits im SCSS.

In `event-calendar.page.ts` die beiden Helfer ergänzen:

```ts
  toIsoString(startingAt: number): string {
    return dayjs(startingAt).toISOString();
  }

  /** Leerstring für Ganztages-Einträge, damit die Zeile dort entfällt. */
  formatTime(startingAt: number): string {
    const date = dayjs(startingAt);
    if (date.hour() === 0 && date.minute() === 0) {
      return '';
    }
    return date.locale('de').format('HH:mm');
  }
```

- [ ] **Step 6: Move the h1 to the top of the content**

In `event-calendar.page.html` den `<h1>Veranstaltungen in {{ namedLatLon.place }}</h1>` vor `<app-inline-calendar>` ziehen, sodass er das erste Element in `<main>` ist.

- [ ] **Step 7: Run tests and verify manually**

Run: `cd packages/frontend && npx nx test upcoming`
Expected: PASS.

```bash
curl -s http://localhost:4200/events/in/CH/ZG/Zug | grep -o '<h[1-6]' | head -5
curl -s http://localhost:4200/events/in/CH/ZG/Zug | grep -c 'bot-only'
```

Expected: `<h1` steht an erster Stelle, und `bot-only` kommt null Mal vor.

- [ ] **Step 8: Commit**

```bash
git add packages/frontend/apps/upcoming/src/app
git commit -m "fix(frontend): lead with the h1, drop hidden bot markup, show start times"
```

---

### Task 10: Gesamtabnahme

- [ ] **Step 1: Run the full gate**

Run: `./gradlew lint test`
Expected: Exit-Code 0.

- [ ] **Step 2: Check the redirect and status matrix against a production build**

```bash
cd packages/frontend && npx nx build apps:upcoming && node dist/apps/upcoming/server/server.mjs &
sleep 5
for p in \
  /events/in/CH \
  /events/in/CH/ZG \
  /events/in/CH/ZG/Zug \
  "/events/in/CH/ZG/Zug?date=2026-09-12" \
  /events/in/CH/ZG/Zug/heute \
  /events/in/CH/ZG/Zug/am/2026/09/08 \
  /events/in/CH/XX/Nirgendwo \
  /events/in/XX ; do
  printf "%-45s " "$p"
  curl -s -o /dev/null -w "%{http_code} %{redirect_url}\n" "http://localhost:4000$p"
done
```

Expected: `200` für die ersten vier, `301` für die beiden Alt-URLs, `404` für die beiden unbekannten Orte.

- [ ] **Step 3: Open a pull request against develop**

```bash
git push -u origin feature/upcoming-url-konsolidierung
gh pr create --base develop \
  --title "feat(frontend): consolidate upcoming urls onto stable place pages" \
  --body "Setzt Block A aus docs/superpowers/specs/2026-09-09-upcoming-url-konsolidierung-design.md um."
```

---

## Nach dem Merge

Die Umstellung ist ein 301 auf rund 2247 indexierte URLs. In der Search Console wird es einige Wochen unruhig, und der Traffic kann sinken, bevor er steigt. Beobachten, nicht sofort gegensteuern.

Nicht Teil dieses Plans, bewusst: Block B (Orts-Cluster, wartet auf den Source-Fix), Block C (Event-Detailseiten), das Impressum als crawlbare Seite, Slugs statt prozent-kodierter Ortsnamen, der Wochenend-Filter auf der Ortsseite, und `og:image` — Befund 5 der Spec bleibt offen, bis ein Bild vorliegt.
