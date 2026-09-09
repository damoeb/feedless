export type SitemapPlace = {
  place: string;
  area: string;
};

export type SitemapLink = {
  url: string;
  changefreq: 'daily' | 'weekly' | 'monthly' | 'yearly';
  lastmod: string;
  priority: number;
};

/**
 * Die Ortsliste kommt als Parameter herein statt als Import: der Generator
 * läuft als reines Node-Skript ausserhalb der Angular-Auflösung, die Tests
 * laufen unter vitest. Eine Funktion ohne Importe ist in beiden Welten gleich
 * benutzbar.
 *
 * Anders als `renderPlaceUrl` wird hier kodiert - das Ergebnis ist ein String
 * für die XML-Datei, keine Eingabe für den Angular-Router.
 */
export function buildSitemapLinks(
  places: SitemapPlace[],
  lastMod: string,
): SitemapLink[] {
  const links: SitemapLink[] = [
    { url: '/', changefreq: 'daily', lastmod: lastMod, priority: 1.0 },
    {
      url: '/ueber-uns/',
      changefreq: 'monthly',
      lastmod: lastMod,
      priority: 0.8,
    },
    { url: '/agb/', changefreq: 'yearly', lastmod: lastMod, priority: 0.3 },
    {
      url: '/events/in/CH',
      changefreq: 'weekly',
      lastmod: lastMod,
      priority: 0.9,
    },
  ];

  const regions = [
    ...new Set(places.map((place) => place.area.toUpperCase())),
  ].sort();
  for (const region of regions) {
    links.push({
      url: `/events/in/CH/${region}`,
      changefreq: 'weekly',
      lastmod: lastMod,
      priority: 0.8,
    });
  }

  for (const location of places) {
    links.push({
      url: `/events/in/CH/${location.area.toUpperCase()}/${encodeURIComponent(
        location.place,
      )}`,
      changefreq: 'daily',
      lastmod: lastMod,
      priority: 0.9,
    });
  }

  return links;
}
