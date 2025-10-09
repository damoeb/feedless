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

    // Enhanced sitemap for upcoming vertical
    if (app.id === 'upcoming') {
      links = this.generateUpcomingSitemapLinks(domain, lastMod);
    }

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
    const domain = `https://${app.domain}`;

    let robotsContent: string;

    if (app.id === 'upcoming') {
      // Enhanced robots.txt for upcoming vertical
      robotsContent = `User-agent: *
Allow: /
Allow: /events/
Allow: /ueber-uns/
Allow: /agb/

# Disallow admin/management areas
Disallow: /management/
Disallow: /login

# Allow all search engines to access events
Allow: /events/in/

Sitemap: ${domain}/sitemap.xml

# Crawl-delay for respectful crawling
Crawl-delay: 1

# Additional instructions for specific bots
User-agent: Googlebot
Crawl-delay: 0

User-agent: Bingbot
Crawl-delay: 1`;
    } else {
      // Default robots.txt for other verticals
      const allowed = app.links.filter((link) => link.allow).map((l) => l.url);
      const disallowed = app.links.filter((link) => !link.allow).map((l) => l.url) || [''];

      robotsContent = `User-agent: *
${allowed.map((url) => `Allow: ${url}`).join('\n')}
${disallowed.map((url) => `Disallow: ${url}`).join('\n')}

Sitemap: ${domain}/sitemap.xml
`;
    }

    this.writeFile(join(outDir, `robots.txt`), robotsContent);
  }

  private generateIndex(app: VerticalSpec, outDir: string, index: string) {
    const api = `https://api.${app.domain}`;
    let enhancedIndex = index;

    if (app.id === 'upcoming') {
      // Enhanced SEO for upcoming vertical
      const seoConfig = this.getUpcomingSEOConfig(app);
      const metaTags = this.generateUpcomingMetaTags(app, seoConfig);
      const structuredData = this.generateUpcomingStructuredData(app);

      enhancedIndex = index
        .replace('<!-- FEEDLESS_META -->', metaTags)
        .replace('<!-- FEEDLESS_TITLE -->', app.title)
        .replace(
          '<meta name="description" content="">',
          `<meta name="description" content="${seoConfig.description}">`
        )
        .replace('</head>', `  ${structuredData}\n  </head>`);
    } else {
      // Default implementation for other verticals
      const data = `<link rel="preconnect" href="${api}">`;
      enhancedIndex = index
        .replace('<!-- FEEDLESS_META -->', data)
        .replace('<!-- FEEDLESS_TITLE -->', app.title)
        .replace(
          '<meta name="description" content="">',
          `<meta name="description" content="${app.summary}">`
        );
    }

    this.writeFile(join(outDir, `index.html`), enhancedIndex);
  }

  private getUpcomingSEOConfig(app: VerticalSpec): SEOConfig & { description: string } {
    return {
      description:
        'Entdecke lokale Veranstaltungen und Events in deiner Nähe. Von Familien-Events über Sport-Aktivitäten bis hin zu kulturellen Veranstaltungen - finde spannende Events in der Schweiz.',
      keywords: [
        'lokale Events',
        'Veranstaltungen',
        'Schweiz',
        'Events heute',
        'lokale.events',
        'Familien Events',
        'Sport Events',
        'Kultur Events',
        'Konzerte',
        'Märkte',
        'Workshops',
        'Nachbarschaftsfeste',
        'Event Kalender',
        'lokale Aktivitäten',
      ],
      ogImage: `https://${app.domain}/assets/upcoming.jpeg`,
      twitterCard: 'summary_large_image',
      structuredData: {
        '@context': 'https://schema.org',
        '@type': 'WebSite',
        name: app.title,
        description: app.summary,
        url: `https://${app.domain}`,
        potentialAction: {
          '@type': 'SearchAction',
          target: {
            '@type': 'EntryPoint',
            urlTemplate: `https://${app.domain}/events/in/{search_term_string}`,
          },
          'query-input': 'required name=search_term_string',
        },
        publisher: {
          '@type': 'Organization',
          name: 'lokale.events',
          url: `https://${app.domain}`,
          logo: {
            '@type': 'ImageObject',
            url: `https://${app.domain}/assets/icons/icon-512x512.png`,
          },
        },
      },
      additionalMeta: [
        { name: 'author', content: 'lokale.events Team' },
        { name: 'robots', content: 'index, follow, max-image-preview:large' },
        {
          name: 'googlebot',
          content: 'index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1',
        },
        { property: 'og:locale', content: 'de_DE' },
        { property: 'og:site_name', content: 'lokale.events' },
        { name: 'twitter:site', content: '@lokale_events' },
        { name: 'twitter:creator', content: '@lokale_events' },
        { name: 'geo.region', content: 'CH' },
        { name: 'geo.placename', content: 'Schweiz' },
        { name: 'geo.position', content: '46.8182;8.2275' },
        { name: 'ICBM', content: '46.8182, 8.2275' },
      ],
    };
  }

  private generateUpcomingMetaTags(
    app: VerticalSpec,
    seoConfig: SEOConfig & { description: string }
  ): string {
    const domain = `https://${app.domain}`;
    const api = `https://api.${app.domain}`;

    const metaTags = [
      // Performance and connectivity
      `<link rel="preconnect" href="${api}">`,
      `<link rel="preconnect" href="https://fonts.googleapis.com">`,
      `<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>`,
      `<link rel="dns-prefetch" href="${api}">`,

      // SEO Meta Tags
      `<meta name="keywords" content="${seoConfig.keywords.join(', ')}">`,
      `<meta name="author" content="lokale.events Team">`,
      `<meta name="robots" content="index, follow, max-image-preview:large">`,
      `<meta name="googlebot" content="index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1">`,

      // Open Graph
      `<meta property="og:type" content="website">`,
      `<meta property="og:title" content="${app.title} | Lokale Events und Veranstaltungen in der Schweiz">`,
      `<meta property="og:description" content="${seoConfig.description}">`,
      `<meta property="og:url" content="${domain}">`,
      `<meta property="og:site_name" content="lokale.events">`,
      `<meta property="og:locale" content="de_DE">`,
      `<meta property="og:image" content="${seoConfig.ogImage}">`,
      `<meta property="og:image:width" content="1200">`,
      `<meta property="og:image:height" content="630">`,
      `<meta property="og:image:alt" content="lokale.events - Entdecke lokale Veranstaltungen">`,

      // Twitter Card
      `<meta name="twitter:card" content="${seoConfig.twitterCard}">`,
      `<meta name="twitter:site" content="@lokale_events">`,
      `<meta name="twitter:creator" content="@lokale_events">`,
      `<meta name="twitter:title" content="${app.title} | Lokale Events und Veranstaltungen">`,
      `<meta name="twitter:description" content="${seoConfig.description}">`,
      `<meta name="twitter:image" content="${seoConfig.ogImage}">`,
      `<meta name="twitter:image:alt" content="lokale.events - Entdecke lokale Veranstaltungen">`,

      // Geo Tags
      `<meta name="geo.region" content="CH">`,
      `<meta name="geo.placename" content="Schweiz">`,
      `<meta name="geo.position" content="46.8182;8.2275">`,
      `<meta name="ICBM" content="46.8182, 8.2275">`,

      // Canonical URL
      `<link rel="canonical" href="${domain}">`,

      // Language alternatives (if you plan to add other languages)
      `<link rel="alternate" hreflang="de" href="${domain}">`,
      `<link rel="alternate" hreflang="x-default" href="${domain}">`,

      // Additional performance hints
      `<link rel="preload" href="/assets/icons/icon-192x192.png" as="image">`,
    ];

    return metaTags.join('\n    ');
  }

  private generateUpcomingStructuredData(app: VerticalSpec): string {
    const domain = `https://${app.domain}`;

    const structuredData = {
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      name: app.title,
      description: app.summary,
      url: domain,
      potentialAction: {
        '@type': 'SearchAction',
        target: {
          '@type': 'EntryPoint',
          urlTemplate: `${domain}/events/in/{search_term_string}`,
        },
        'query-input': 'required name=search_term_string',
      },
      publisher: {
        '@type': 'Organization',
        name: 'lokale.events',
        url: domain,
        logo: {
          '@type': 'ImageObject',
          url: `${domain}/assets/icons/icon-512x512.png`,
          width: 512,
          height: 512,
        },
        sameAs: [] as string[],
        // Add social media profiles when available
        // 'https://twitter.com/lokale_events',
        // 'https://facebook.com/lokale.events'
      },
      mainEntity: {
        '@type': 'ItemList',
        name: 'Lokale Veranstaltungen',
        description: 'Liste der aktuellen lokalen Veranstaltungen und Events in der Schweiz',
        numberOfItems: 'varies',
      },
    };

    return `<script type="application/ld+json">\n${JSON.stringify(structuredData, null, 2)}\n  </script>`;
  }

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'build/generated'));
