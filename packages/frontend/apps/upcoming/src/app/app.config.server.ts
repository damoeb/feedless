import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { SERVER_APP_CONFIG } from '@feedless/components';
import { VerticalAppConfig } from '@feedless/core';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';

/**
 * Read config.json from disk rather than over HTTP: on the server there is no
 * origin to resolve '/config.json' against, and prerendering runs before any
 * server is listening. In the container the file is /app/browser/config.json
 * (mounted from a ConfigMap), during a build it is the app's public/ copy —
 * set APP_CONFIG_JSON to prerender against a different config.
 */
function readServerAppConfig(): VerticalAppConfig {
  const here = dirname(fileURLToPath(import.meta.url));
  const candidates = [
    process.env['APP_CONFIG_JSON'],
    resolve(here, '../browser/config.json'),
    resolve(here, 'config.json'),
    resolve(process.cwd(), 'apps/upcoming/public/config.json'),
    resolve(process.cwd(), 'public/config.json'),
  ];
  for (const candidate of candidates.filter(Boolean) as string[]) {
    try {
      return JSON.parse(readFileSync(candidate, 'utf-8'));
    } catch {
      // try the next location
    }
  }
  throw new Error(`Cannot read config.json, tried: ${candidates.join(', ')}`);
}

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    { provide: SERVER_APP_CONFIG, useFactory: readServerAppConfig },
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
