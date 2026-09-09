import dayjs from 'dayjs';
import {
  parseDateFromQuery,
  parseEventIdFromSlug,
  renderEventUrl,
  toEventSlug,
  renderPlaceUrl,
  upcomingBaseRoute,
} from './upcoming-product-routes';
import { parsePath, renderPath } from 'typesafe-routes';

describe('upcomingBaseRoute', () => {
  it('renders the place route', () => {
    const url = renderPath(upcomingBaseRoute.events.countryCode.region.place, {
      countryCode: 'CH',
      region: 'AR',
      place: 'Place',
    });
    expect(url).toEqual('/events/in/CH/AR/Place');
  });

  it('parses the place route', () => {
    const { countryCode, region, place } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place,
      '/events/in/CH/ZH/Thalwil',
    );

    expect(countryCode).toEqual('CH');
    expect(region).toEqual('ZH');
    expect(place).toEqual('Thalwil');
  });
});

describe('parseDateFromQuery', () => {
  it('falls back to today when no date is given', () => {
    const { date, explicit } = parseDateFromQuery({});
    expect(date.format('YYYY-MM-DD')).toBe(dayjs().format('YYYY-MM-DD'));
    expect(explicit).toBe(false);
  });

  it('reads an ISO date', () => {
    const { date, explicit } = parseDateFromQuery({ date: '2026-09-12' });
    expect(date.format('YYYY-MM-DD')).toBe('2026-09-12');
    expect(explicit).toBe(true);
  });

  it('falls back to today for an unparsable date', () => {
    const { date, explicit } = parseDateFromQuery({ date: 'gestern' });
    expect(date.format('YYYY-MM-DD')).toBe(dayjs().format('YYYY-MM-DD'));
    expect(explicit).toBe(false);
  });

  it('rejects a date that is not zero padded', () => {
    const { explicit } = parseDateFromQuery({ date: '2026-9-1' });
    expect(explicit).toBe(false);
  });
});

describe('renderPlaceUrl', () => {
  it('renders the place url', () => {
    expect(renderPlaceUrl('CH', 'ZG', 'Zug')).toBe('/events/in/CH/ZG/Zug');
  });

  /**
   * Bewusst unkodiert: das Ergebnis geht an `routerLink` und `navigateByUrl`,
   * und Angular kodiert beim Serialisieren. Eine hier schon kodierte URL käme
   * als `%2520` beim Nutzer an. Der Sitemap-Generator kodiert separat, weil er
   * Strings baut statt Router-Eingaben.
   */
  it('leaves encoding to the angular url serializer', () => {
    expect(renderPlaceUrl('CH', 'AG', 'Aarau Rohr')).toBe(
      '/events/in/CH/AG/Aarau Rohr',
    );
  });
});

describe('toEventSlug', () => {
  const id = '754bec48-e44b-4818-b5c1-6a017bef09a1';

  it('puts a readable slug in front of the id', () => {
    expect(toEventSlug('Chilbi Baar', id)).toBe(`chilbi-baar-${id}`);
  });

  it('folds umlauts and accents', () => {
    expect(toEventSlug('Zämä bewegä', id)).toBe(`zaemae-bewegae-${id}`);
    expect(toEventSlug('Fête de la Musique', id)).toBe(
      `fete-de-la-musique-${id}`,
    );
  });

  it('falls back to the bare id when the title yields nothing', () => {
    expect(toEventSlug('', id)).toBe(id);
    expect(toEventSlug('!!! ???', id)).toBe(id);
  });

  it('caps the slug and never ends it on a separator', () => {
    const slug = toEventSlug('a'.repeat(200), id);
    expect(slug).toBe(`${'a'.repeat(60)}-${id}`);
    expect(toEventSlug('Sport ' + 'x'.repeat(60), id)).not.toContain('--');
  });
});

describe('parseEventIdFromSlug', () => {
  const id = '754bec48-e44b-4818-b5c1-6a017bef09a1';

  /**
   * Titel-Slug und uuid enthalten beide Bindestriche - die id muss deshalb am
   * Ende verankert gelesen werden, nicht am ersten Trenner.
   */
  it('reads the id out of a slug that is full of hyphens', () => {
    expect(parseEventIdFromSlug(`chilbi-baar-am-see-${id}`)).toBe(id);
    expect(parseEventIdFromSlug(id)).toBe(id);
  });

  it('returns null when no id is present', () => {
    expect(parseEventIdFromSlug('chilbi-baar')).toBeNull();
    expect(parseEventIdFromSlug(undefined)).toBeNull();
  });
});

describe('renderEventUrl', () => {
  it('renders the event url below its place', () => {
    expect(
      renderEventUrl('CH', 'ZG', 'Zug', {
        id: '754bec48-e44b-4818-b5c1-6a017bef09a1',
        title: 'Chilbi Baar',
      }),
    ).toBe(
      '/events/in/CH/ZG/Zug/e/chilbi-baar-754bec48-e44b-4818-b5c1-6a017bef09a1',
    );
  });
});
