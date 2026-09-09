import dayjs from 'dayjs';
import { safeParsePath } from 'typesafe-routes';
import { upcomingBaseRoute } from './app/upcoming-product-routes';
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
  const locations = getCachedLocations();
  const inCountry = locations.filter(
    (location) =>
      normalize(location.countryCode) === normalize(path.countryCode),
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

export type OutdatedResult =
  | { outdated: false }
  | {
      outdated: true;
      params?: {
        day: number;
        month: number;
        year: number;
        countryCode: string;
        region: string;
        place: string;
      };
    };

export function checkOutdated(path: string): OutdatedResult {
  const parsedRoute = safeParsePath(
    upcomingBaseRoute.events.countryCode.region.place.dateTime,
    path,
  );
  if (parsedRoute.success) {
    const maxAge = dayjs().subtract(7, 'days');
    const { day, month, year, countryCode, region, place } = parsedRoute.data;
    const routeDate = dayjs()
      .year(year)
      .month(month - 1)
      .date(day);
    return {
      outdated: routeDate.isAfter(maxAge),
      params: { day, month, year, countryCode, region, place },
    };
  }

  return { outdated: false };
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
