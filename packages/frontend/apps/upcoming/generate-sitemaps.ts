import { SitemapStream, streamToPromise } from 'sitemap';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { Readable } from 'node:stream';
// A plain node build script, not app code: it runs before the Angular
// toolchain, so it cannot resolve the @feedless/geo path alias, and the
// barrel would pull in Angular services it has no injector for.
// eslint-disable-next-line @nx/enforce-module-boundaries
import { getCachedLocations } from '../../libs/geo/src/lib/places.ts';

// Single source of truth: the same list the app resolves urls against. This was
// a ~740-line hand-maintained copy, which only stays correct for as long as
// someone remembers to edit both.
const places: { place: string; area: string }[] = [
  ...new Map(
    getCachedLocations().map((l) => [`${l.area}/${l.place}`, l]),
  ).values(),
]
  .map(({ place, area }) => ({ place, area }))
  .sort((a, b) =>
    `${a.area}/${a.place}`.localeCompare(`${b.area}/${b.place}`),
  );

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
      this.writeFile(
        join(outDir, `sitemap.xml`),
        this.prettyPrint(String(sitemap)),
      ),
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
    ]; // Add location-based event pages with better priorities
    const locationUrls = places.map((location) => {
      return `CH/${location.area.toUpperCase()}/${encodeURIComponent(location.place)}`;
    });
    locationUrls.forEach((location) => {
      ['heute', 'morgen', 'kommendes-wochenende'].forEach((day) => {
        const dateUrl = `/events/in/${location}/${day}`;
        links.push({
          url: dateUrl,
          changefreq: 'daily' as const,
          lastmod: lastMod,
          priority: day != 'kommendes-wochenende' ? 0.9 : 0.7, // Higher priority for today
        });
      });
    });
    return links;
  }

  // One element per line, so a regenerated sitemap produces a reviewable diff
  // instead of one 400KB line.
  private prettyPrint(xml: string): string {
    let depth = 0;
    return (
      xml
        .replace(/></g, '>\n<')
        .split('\n')
        .map((line) => {
          if (/^<\/\w/.test(line)) depth--;
          const indented = '  '.repeat(Math.max(depth, 0)) + line;
          if (/^<\w[^>]*[^/]>$/.test(line) && !/^<\?/.test(line)) depth++;
          return indented;
        })
        .join('\n') + '\n'
    );
  }

  private writeFile(file: string, data: string) {
    console.log(`* ${file}`);
    writeFileSync(file, data);
  }
}

new AppsDataGenerator(join(process.cwd(), 'apps/upcoming/public'));
