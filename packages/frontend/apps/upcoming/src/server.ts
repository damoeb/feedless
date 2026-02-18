import { AngularNodeAppEngine, createNodeRequestHandler, isMainModule, writeResponseToNodeResponse } from '@angular/ssr/node';
import express from 'express';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { checkOutdated, createAccessLogLine } from './server-utils';
import { renderPath } from 'typesafe-routes';
import { upcomingBaseRoute } from './app/upcoming-product-routes';

const app = express();
const angularApp = new AngularNodeAppEngine();

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
createRequestLogger();
serveStatic();

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use('/**', (req, res, next) => {
  const outdated = checkOutdated(req.path);
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
