import { SitemapStream, streamToPromise } from 'sitemap';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { Readable } from 'node:stream';

class AppsDataGenerator {
  constructor(buildFolder: string) {
    console.log('buildFolder:', buildFolder);

    this.generateSiteMap(buildFolder);
  }

  private generateSiteMap(outDir: string) {
    const domain = `https://lokale.events/`;
    const lastMod = new Date().toISOString();

    const links = this.generateUpcomingSitemapLinks(lastMod);

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
  }

  private generateUpcomingSitemapLinks(lastMod: string): Array<{
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
      '/events/in/CH/ZH/Hedingen/',
    ];

    locationUrls.forEach((url) => {
      // Add current date and next few days for each location
      const today = new Date();
      for (let i = 0; i < 7; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);

        ['heute', 'morgen', 'kommendes-wochenende'].forEach((day) => {
          const dateUrl = `${url}${day}`;
          links.push({
            url: dateUrl,
            changefreq: 'daily' as const,
            lastmod: lastMod,
            priority: i === 0 ? 0.9 : 0.7 - i * 0.05, // Higher priority for today
          });
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

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'apps/upcoming/public'));
