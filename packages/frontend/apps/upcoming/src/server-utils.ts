import { Request, Response } from 'express';
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
  const { countryCode, region, place } = path;
  const inCountry = getCachedLocations().filter(
    (location) => normalize(location.countryCode) === normalize(countryCode),
  );
  if (inCountry.length === 0) {
    return false;
  }
  if (!region) {
    return true;
  }
  const inRegion = inCountry.filter(
    (location) => normalize(location.area) === normalize(region),
  );
  if (inRegion.length === 0) {
    return false;
  }
  if (!place) {
    return true;
  }
  return inRegion.some(
    (location) => normalize(location.place) === normalize(place),
  );
}

/**
 * Zwei Lesarten desselben Namens: die deutsche Umlaut-Auflösung (Zürich →
 * zuerich), die der SearchServer in `detail` verwendet, und die reine
 * Diakritika-Entfernung (Zürich → zurich). Verglichen wird gegen beide.
 */
function normalizeForCompare(value: string): string[] {
  const lower = value.toLowerCase();
  const expanded = lower
    .replace(/ä/g, 'ae')
    .replace(/ö/g, 'oe')
    .replace(/ü/g, 'ue')
    .replace(/ß/g, 'ss');
  const stripped = lower.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  return [expanded, stripped].map((variant) =>
    variant.replace(/[^a-z0-9]+/g, ' ').trim(),
  );
}

/**
 * Der SearchServer sucht unscharf und liefert praktisch immer ein Ergebnis -
 * `Quatschort XX` ergibt `Le Sex Blanc`. „Es kam etwas zurück" taugt deshalb
 * nicht als Prüfung; der Treffer muss den angefragten Ort auch tragen.
 */
export function detailMatchesPlace(place: string, detail: string): boolean {
  const haystack = ` ${normalizeForCompare(detail).join(' ')} `;
  return normalizeForCompare(place).some(
    (variant) => variant.length > 0 && haystack.includes(` ${variant} `),
  );
}

export type LocationLookup = (path: EventsPath) => Promise<boolean>;

/**
 * Die gebündelte Ortsliste deckt nur sechs Kantone ab und kennt Kantone nur
 * unter ihrem Kürzel. `/events/in/CH/Zürich/Hedingen` und `/events/in/CH/BE/Bern`
 * sind trotzdem echte Orte, die über die admin.ch-Suche auflösen - ein 404
 * allein auf Basis der Liste würde funktionierende Seiten abschalten.
 *
 * Deshalb: Listentreffer sofort durchlassen, sonst nachfragen. Schlägt die
 * Abfrage fehl, wird durchgelassen statt geblockt - eine Störung bei admin.ch
 * darf nicht die halbe Schweiz auf 404 setzen.
 */
export async function isResolvableLocation(
  path: EventsPath,
  lookup: LocationLookup,
): Promise<boolean> {
  if (isKnownLocation(path)) {
    return true;
  }
  // Die Hub-Ebenen rendern ausschliesslich aus der Liste. Ein Kanton, der
  // nicht darin steht, hat dort nichts zu zeigen.
  if (!path.region || !path.place) {
    return false;
  }
  try {
    return await lookup(path);
  } catch {
    return true;
  }
}

const RELATIVE_DATE_KEYWORDS = [
  'gestern',
  'heute',
  'morgen',
  'kommendes-wochenende',
];

const placeUrl = (countryCode: string, region: string, place: string): string =>
  `/events/in/${encodeURIComponent(countryCode)}/${encodeURIComponent(
    region,
  )}/${encodeURIComponent(place)}`;

const pad = (value: string): string => value.padStart(2, '0');

/**
 * Liefert das 301-Ziel für eine Alt-URL, sonst null.
 *
 * Die relativen Datums-Pfade zeigen bewusst auf die nackte Ortsseite und nicht
 * auf ein berechnetes `?date=`: ein 301 wird dauerhaft zwischengespeichert, ein
 * relatives Datum ändert sich täglich. Nur `/am/` trägt ein festes Datum.
 */
export function getLegacyRedirect(path: EventsPath): string | null {
  const { countryCode, region, place, rest } = path;
  if (!region || !place || rest.length === 0) {
    return null;
  }
  const base = placeUrl(countryCode, region, place);
  const [head, ...tail] = rest;

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

/**
 * Sekunden bis zum nächsten UTC-Mitternacht, mindestens eine. Der Inhalt einer
 * Ortsseite wechselt genau einmal pro Tag; ohne Cache-Header hängt jeder Crawl
 * am SSR-Rendering.
 */
export function secondsUntilMidnight(now: Date): number {
  const midnight = Date.UTC(
    now.getUTCFullYear(),
    now.getUTCMonth(),
    now.getUTCDate() + 1,
  );
  return Math.max(1, Math.round((midnight - now.getTime()) / 1000));
}

export type RequestLog = {
  clientIp: string;
  timestamp: string;
  method: string;
  httpVersion: string;
  url: string;
  status: number;
  referer: string;
  userAgent: string;
  duration: string;
};

export function createAccessLogLine(req: Request, res: Response): RequestLog {
  const start = Date.now();
  const clientIp =
    (req.headers['x-forwarded-for'] as string)?.split(',')[0]?.trim() ||
    req.socket?.remoteAddress ||
    '-';
  const duration = Date.now() - start;
  const referer = String(
    req.headers.referer ?? req.headers['referrer'] ?? '-',
  ).replace(/"/g, '\\"');
  const userAgent = String(req.headers['user-agent'] ?? '-').replace(
    /"/g,
    '\\"',
  );

  return {
    clientIp,
    timestamp: `[${new Date().toISOString()}]`,
    method: req.method,
    url: req.originalUrl || req.url,
    httpVersion: `HTTP/${req.httpVersionMajor}.${req.httpVersionMinor}"`,
    status: res.statusCode,
    referer: `"${referer}"`,
    userAgent: `"${userAgent}"`,
    duration: `${duration}ms`,
  };
}
