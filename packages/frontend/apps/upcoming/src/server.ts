import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import compression from 'compression';
import express, { NextFunction, Request, Response } from 'express';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  createAccessLogLine,
  detailMatchesPlace,
  EventsPath,
  getEventQueryRedirect,
  getLegacyRedirect,
  isResolvableLocation,
  parseEventsPath,
  secondsUntilMidnight,
} from './server-utils';
import { SEARCH_SERVER } from '@feedless/geo';
import { isDevMode } from '@angular/core';

const app = express();
app.use(compression());
const angularApp = new AngularNodeAppEngine();

const LOKALE_EVENTS_HOST = 'lokale.events';
const LOKALE_EVENTS_WWW = 'www.lokale.events';

/** Redirect HTTP→HTTPS and www→non-www when not behind K8s ingress (e.g. dev or direct deploy). */
function redirectMiddleware(req: Request, res: Response, next: NextFunction) {
  const host = (req.header('x-forwarded-host') || req.get('host') || '').split(
    ':',
  )[0];
  if (host !== LOKALE_EVENTS_HOST && host !== LOKALE_EVENTS_WWW) {
    return next();
  }
  const proto =
    req.header('x-forwarded-proto') ?? (req.secure ? 'https' : 'http');
  const path = req.originalUrl || req.url;
  if (host === LOKALE_EVENTS_WWW) {
    return res.redirect(301, `https://${LOKALE_EVENTS_HOST}${path}`);
  }
  if (proto === 'http') {
    return res.redirect(301, `https://${host}${path}`);
  }
  next();
}

function createRequestLogger() {
  app.use((req, res, next) => {
    res.on('finish', () => {
      const requestLog = createAccessLogLine(req, res);
      if (!requestLog.url.endsWith('.js')) {
        process.stdout.write(Object.values(requestLog).join(' ') + '\n');
      }
    });
    next();
  });
}

function serveStatic() {
  const serverDistFolder = dirname(fileURLToPath(import.meta.url));
  const browserDistFolder = resolve(serverDistFolder, '../browser');
  app.use(
    express.static(browserDistFolder, {
      maxAge: '1y',
      index: false,
      redirect: false,
    }),
  );
}

/**
 * Ergebnisse der admin.ch-Rückfrage, damit ein Crawler, der dieselbe unbekannte
 * URL wiederholt anfragt, nicht jedes Mal eine Anfrage auslöst.
 */
const locationLookupCache = new Map<string, boolean>();

async function lookupLocation(path: EventsPath): Promise<boolean> {
  const key = `${path.countryCode}/${path.region}/${path.place}`;
  const cached = locationLookupCache.get(key);
  if (cached !== undefined) {
    return cached;
  }
  const params = new URLSearchParams({
    searchText: [path.place, path.region].filter(Boolean).join(' '),
    type: 'locations',
    sr: '4326',
    limit: '5',
  });
  const response = await fetch(`${SEARCH_SERVER}?${params.toString()}`, {
    signal: AbortSignal.timeout(3000),
  });
  if (!response.ok) {
    throw new Error(`SearchServer answered ${response.status}`);
  }
  const body = (await response.json()) as {
    results?: { attrs?: { detail?: string } }[];
  };
  const resolvable = (body.results ?? []).some((result) =>
    detailMatchesPlace(path.place ?? '', result.attrs?.detail ?? ''),
  );
  locationLookupCache.set(key, resolvable);
  return resolvable;
}

function handleSignals() {
  process.on('SIGINT', () => {
    console.log('SIGINT received. Shutting down...');
    process.exit(0);
  });

  process.on('SIGTERM', () => {
    console.log('SIGTERM received. Shutting down...');
    process.exit(0);
  });
}

handleSignals();
if (!isDevMode()) {
  app.use(redirectMiddleware);
}
createRequestLogger();
serveStatic();

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use('/**', async (req, res, next) => {
  // `/**` ist für express ein Mount-Präfix: innerhalb des Handlers ist
  // `req.path` immer `/` und der tatsächliche Pfad steht in `req.baseUrl`.
  // `originalUrl` ist die einzige Quelle, die ungekürzt bleibt.
  const pathname = (req.originalUrl || req.url).split('?')[0];
  const eventsPath = parseEventsPath(pathname);

  if (eventsPath) {
    const redirect = getLegacyRedirect(eventsPath);
    if (redirect) {
      return res.redirect(301, redirect);
    }
    const eventQuery = getEventQueryRedirect(
      eventsPath,
      new URL(req.originalUrl || req.url, 'http://localhost').searchParams.get(
        'event',
      ) ?? undefined,
    );
    if (eventQuery) {
      return res.redirect(301, eventQuery);
    }
    if (!(await isResolvableLocation(eventsPath, lookupLocation))) {
      return res.status(404).send('Not found');
    }
  }

  // Der Seiteninhalt wechselt einmal pro Tag. `max-age=0` hält den
  // Browser-Cache aussen vor, `s-maxage` gilt für CDN und Reverse Proxy, und
  // `stale-while-revalidate` verhindert, dass um Mitternacht alle Kanten
  // gleichzeitig auf den SSR durchschlagen.
  res.setHeader(
    'Cache-Control',
    `public, max-age=0, s-maxage=${secondsUntilMidnight(new Date())}, stale-while-revalidate=3600`,
  );

  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
