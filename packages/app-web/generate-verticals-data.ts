import { SitemapStream, streamToPromise } from 'sitemap';
import { allVerticals, VerticalSpec } from './src/app/all-verticals';
import { existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'fs';
import { join } from 'path';
import { VerticalAppConfig } from './src/app/types';
import { Readable } from 'node:stream';

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
      this.generateRobotsTxt(app, outDir);
      this.generateAppConfig(app, outDir);
      const index = String(readFileSync('www/browser/index.html'));
      this.generateIndex(app, outDir, index);
    }
  }

  private generateSiteMap(app: VerticalSpec, outDir: string) {
    const domain = `https://${app.domain}/`;
    const lastMod = new Date().toISOString();

    const links = app.links
      .filter((l) => l.allow)
      .map((l) => ({
        url: l.url,
        changefreq: 'weekly',
        lastmod: lastMod,
        priority: 0.3,
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
      this.writeFile(join(outDir, `sitemap.xml`), String(sitemap)),
    );

    // const mainPage: SitemapItemLoose = {
    //   url: domain,
    //   lastmod: lastMod,
    //   priority: 0.8,
    // };
    // smStream.write(mainPage);
    //
    // smStream.end();
  }

  private generateAppConfig(app: VerticalSpec, outDir: string) {
    const appConfig: VerticalAppConfig = {
      apiUrl: `https://api.${app.domain}`,
      product: app.product,
      attributionHtml: 'Whoever loves discipline loves knowledge',
      offlineSupport: app.offlineSupport,
    };
    this.writeFile(
      join(outDir, `config.json`),
      JSON.stringify(appConfig, null, 2),
    );
  }

  private createFeedlessConfig() {
    type ProductId2AppConfig = { [k: string]: VerticalSpec };
    const apps: ProductId2AppConfig = allVerticals.verticals.reduce(
      (acc, app) => {
        acc[app.id] = app;
        return acc;
      },
      {} as ProductId2AppConfig,
    );

    const config = JSON.parse(JSON.stringify(allVerticals));
    config.apps = apps;

    this.writeFile('./all-verticals.json', JSON.stringify(config, null, 2));
  }

  private generateRobotsTxt(app: VerticalSpec, outDir: string) {
    const domain = `https://${app.domain}`;
    const allowed = app.links.filter((link) => link.allow).map((l) => l.url);
    const disallowed = app.links
      .filter((link) => !link.allow)
      .map((l) => l.url) || [''];
    const data = `User-agent: *
${allowed.map((url) => `Allow: ${url}`)}
${disallowed.map((url) => `Disallow: ${url}`)}

Sitemap: ${domain}/sitemap.xml
`;
    this.writeFile(join(outDir, `robots.txt`), data);
  }

  private generateIndex(app: VerticalSpec, outDir: string, index: string) {
    const api = `https://api.${app.domain}`;
    const data = `<link rel="preconnect" href="${api}">`;
    this.writeFile(
      join(outDir, `index.html`),
      index
        .replace('<!-- FEEDLESS_META -->', data)
        .replace('<!-- FEEDLESS_TITLE -->', app.title)
        .replace(
          '<meta name="description" content="">',
          `<meta name="description" content="${app.summary}">`,
        ),
    );
  }

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'build/generated'));
