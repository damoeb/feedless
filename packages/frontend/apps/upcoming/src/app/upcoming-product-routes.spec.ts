import dayjs from 'dayjs';
import {
  parseDateFromQuery,
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
