import { SitemapStream, streamToPromise } from 'sitemap';
import { allVerticals, VerticalSpec } from './src/app/all-verticals';
import { existsSync, mkdirSync, rmSync, writeFileSync } from 'fs';
import { join } from 'path';
import { VerticalAppConfig } from './src/app/types';
import { Readable } from 'node:stream';
import { readFileSync } from 'node:fs';

class AppsDataGenerator {
  constructor(buildFolder: string) {
    console.log('buildFolder:', buildFolder);
    this.createFeedlessConfig();

    if (existsSync(buildFolder)) {
      rmSync(buildFolder, { recursive: true });
    }
    mkdirSync(buildFolder, { recursive: true });

    for (let appKey in allVerticals.verticals) {
      const app = allVerticals.verticals[appKey];
      if (!app.domain) {
        continue;
      }
      const outDir = join(buildFolder, app.id);
      mkdirSync(outDir);

      this.generateSiteMap(app, outDir);
      // this.generateRobotsTxt(app, outDir);
      this.generateAppConfig(app, outDir);
      const index = String(readFileSync('www/browser/index.html'));
      this.generateIndex(app, outDir, index);
    }
  }

  private generateSiteMap(app: VerticalSpec, outDir: string) {
    const domain = `https://${app.domain}/`;
    const lastMod = new Date().toISOString();

    type SitemapLink = {
      url: string;
      changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly';
      lastmod: string;
      priority: number;
    };

    let links: SitemapLink[] = app.links
      .filter((l) => l.allow)
      .map((l) => ({
        url: l.url,
        changefreq: 'weekly' as const,
        lastmod: lastMod,
        priority: l.url === '/' ? 1.0 : 0.7,
      }));

    const smStream = new SitemapStream({
      hostname: domain,
      lastmodDateOnly: false,
      xmlns: {
        news: false,
        xhtml: true,
        image: false,
        video: false,
      },
    });

    streamToPromise(Readable.from(links).pipe(smStream)).then((sitemap) =>
      this.writeFile(join(outDir, `sitemap.xml`), String(sitemap))
    );
  }

  private generateAppConfig(app: VerticalSpec, outDir: string) {
    const appConfig: VerticalAppConfig = {
      apiUrl: `https://api.${app.domain}`,
      product: app.product,
      attributionHtml: 'Whoever loves discipline loves knowledge',
      offlineSupport: app.offlineSupport,
    };
    this.writeFile(join(outDir, `config.json`), JSON.stringify(appConfig, null, 2));
  }

  private createFeedlessConfig() {
    type ProductId2AppConfig = { [k: string]: VerticalSpec };
    const apps: ProductId2AppConfig = allVerticals.verticals.reduce((acc, app) => {
      acc[app.id] = app;
      return acc;
    }, {} as ProductId2AppConfig);

    const config = JSON.parse(JSON.stringify(allVerticals));
    config.apps = apps;

    this.writeFile('./all-verticals.json', JSON.stringify(config, null, 2));
  }

  private generateRobotsTxt(app: VerticalSpec, outDir: string) {
    const domain = `https://${app.domain}`;

    let robotsContent: string;

    this.writeFile(join(outDir, `robots.txt`), robotsContent);
  }

  private generateIndex(app: VerticalSpec, outDir: string, index: string) {
    const api = `https://api.${app.domain}`;
    let enhancedIndex = index;

    // Default implementation for other verticals
    const data = `<link rel="preconnect" href="${api}">`;
    enhancedIndex = index
      .replace('<!-- FEEDLESS_META -->', data)
      .replace('<!-- FEEDLESS_TITLE -->', app.title)
      .replace(
        '<meta name="description" content="">',
        `<meta name="description" content="${app.summary}">`
      );

    this.writeFile(join(outDir, `index.html`), enhancedIndex);
  }

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'build/generated'));
