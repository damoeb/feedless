import { SitemapStream, streamToPromise } from 'sitemap';
import { allVerticals, VerticalSpec } from './src/app/all-verticals';
import { existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from 'fs';
import { join } from 'path';
import { VerticalAppConfig } from './src/app/types';
import { Readable } from 'node:stream';

// Enhanced SEO configuration for upcoming vertical
type SEOConfig = {
  keywords: string[];
  ogImage?: string;
  twitterCard: string;
  structuredData?: any;
  additionalMeta?: Array<{ name?: string; property?: string; content: string }>;
};

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

  private generateUpcomingSitemapLinks(
    domain: string,
    lastMod: string
  ): Array<{
    url: string;
    changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly';
    lastmod: string;
    priority: number;
  }> {
    const links = [
      {
        url: '/',
        changefreq: 'daily' as const,
        lastmod: lastMod,
        priority: 1.0,
      },
      {
        url: '/ueber-uns/',
        changefreq: 'monthly' as const,
        lastmod: lastMod,
        priority: 0.8,
      },
      {
        url: '/agb/',
        changefreq: 'yearly' as const,
        lastmod: lastMod,
        priority: 0.3,
      },
    ];

    // Add location-based event pages with better priorities
    const locationUrls = [
      '/events/in/CH/ZH/Affoltern%2520am%2520Albis/',
      '/events/in/CH/ZH/Birmensdorf/',
      '/events/in/CH/ZH/R%25C3%25BCschlikon/',
      '/events/in/CH/ZH/Thalwil/',
      '/events/in/CH/AG/Wohlen%2520AG/',
      '/events/in/CH/ZH/Urdorf/',
    ];

    locationUrls.forEach((url) => {
      // Add current date and next few days for each location
      const today = new Date();
      for (let i = 0; i < 7; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        const dateUrl = `${url}am/${year}/${month}/${day}/innerhalb/10Km`;
        links.push({
          url: dateUrl,
          changefreq: 'daily' as const,
          lastmod: lastMod,
          priority: i === 0 ? 0.9 : 0.7 - i * 0.05, // Higher priority for today
        });
      }

      // Also add the base location URL
      links.push({
        url: url,
        changefreq: 'daily' as const,
        lastmod: lastMod,
        priority: 0.8,
      });
    });

    return links;
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
