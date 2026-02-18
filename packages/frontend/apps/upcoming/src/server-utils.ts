import dayjs from 'dayjs';
import { safeParsePath } from 'typesafe-routes';
import { upcomingBaseRoute } from './app/upcoming-product-routes';
import { Request, Response } from 'express';

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
