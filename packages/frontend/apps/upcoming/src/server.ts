import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express, { Request } from 'express';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { renderPath, safeParsePath } from 'typesafe-routes';
import { upcomingBaseRoute } from './app/upcoming-product-routes';
import dayjs from 'dayjs';

const serverDistFolder = dirname(fileURLToPath(import.meta.url));
const browserDistFolder = resolve(serverDistFolder, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * ```ts
 * app.get('/api/**', (req, res) => {
 *   // Handle API request
 * });
 * ```
 */

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

function checkOutdated(req: Request):
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
    } {
  const maxAge = dayjs().subtract(7, 'days');
  const routeWithoutPerimeter = safeParsePath(
    upcomingBaseRoute.events.countryCode.region.place.dateTime,
    req.path,
  );
  if (routeWithoutPerimeter.success) {
    const { day, month, year, countryCode, region, place } =
      routeWithoutPerimeter.data;
    return {
      outdated: dayjs().year(year).month(month).day(day).isBefore(maxAge),
      params: { day, month, year, countryCode, region, place },
    };
  }

  return { outdated: false };
}

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use('/**', (req, res, next) => {
  const outdated = checkOutdated(req);
  if (outdated.outdated) {
    return res.redirect(
      renderPath(
        upcomingBaseRoute.events.countryCode.region.place.dateTime,
        outdated.params,
      ),
    );
  }

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
