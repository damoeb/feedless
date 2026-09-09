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
