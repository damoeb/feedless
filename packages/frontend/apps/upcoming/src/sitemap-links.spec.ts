import { buildSitemapLinks, SitemapPlace } from './sitemap-links';

describe('buildSitemapLinks', () => {
  const places: SitemapPlace[] = [
    { place: 'Zug', area: 'ZG' },
    { place: 'Baar', area: 'ZG' },
    { place: 'Aarau Rohr', area: 'AG' },
  ];
  const links = buildSitemapLinks(places, '2026-09-09T00:00:00.000Z');
  const urls = links.map((link) => link.url);

  it('contains the static pages', () => {
    expect(urls).toContain('/');
    expect(urls).toContain('/ueber-uns/');
    expect(urls).toContain('/agb/');
  });

  it('contains the country hub and one hub per canton', () => {
    expect(urls).toContain('/events/in/CH');
    expect(urls).toContain('/events/in/CH/ZG');
    expect(urls).toContain('/events/in/CH/AG');
  });

  it('contains one bare url per place, percent-encoded', () => {
    expect(urls).toContain('/events/in/CH/ZG/Zug');
    expect(urls).toContain('/events/in/CH/AG/Aarau%20Rohr');
  });

  it('contains no date urls any more', () => {
    expect(
      urls.filter((url) =>
        /\/(heute|morgen|gestern|kommendes-wochenende)$/.test(url),
      ),
    ).toEqual([]);
    expect(urls.filter((url) => url.includes('/am/'))).toEqual([]);
  });

  it('lists every url exactly once', () => {
    expect(new Set(urls).size).toBe(urls.length);
  });

  it('carries the given lastmod on every entry', () => {
    expect(
      links.every((link) => link.lastmod === '2026-09-09T00:00:00.000Z'),
    ).toBe(true);
  });
});
