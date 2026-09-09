import {
  getLegacyRedirect,
  isKnownLocation,
  parseEventsPath,
} from './server-utils';

describe('parseEventsPath', () => {
  it('returns null for paths outside /events/in', () => {
    expect(parseEventsPath('/ueber-uns')).toBeNull();
    expect(parseEventsPath('/')).toBeNull();
  });

  it('parses country, region, place and the remaining segments', () => {
    expect(parseEventsPath('/events/in/CH/ZG/Zug/heute')).toEqual({
      countryCode: 'CH',
      region: 'ZG',
      place: 'Zug',
      rest: ['heute'],
    });
  });

  it('decodes percent-encoded place names', () => {
    expect(parseEventsPath('/events/in/CH/AG/Aarau%20Rohr')?.place).toBe(
      'Aarau Rohr',
    );
  });

  it('parses the hub levels', () => {
    expect(parseEventsPath('/events/in/CH')).toEqual({
      countryCode: 'CH',
      region: undefined,
      place: undefined,
      rest: [],
    });
    expect(parseEventsPath('/events/in/CH/ZG')).toEqual({
      countryCode: 'CH',
      region: 'ZG',
      place: undefined,
      rest: [],
    });
  });
});

describe('isKnownLocation', () => {
  it('accepts the country hub', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH')!)).toBe(true);
  });

  it('accepts a known region and a known place', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH/ZG')!)).toBe(true);
    expect(isKnownLocation(parseEventsPath('/events/in/CH/ZG/Zug')!)).toBe(
      true,
    );
  });

  it('rejects an unknown country, region or place', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/XX')!)).toBe(false);
    expect(isKnownLocation(parseEventsPath('/events/in/CH/QQ')!)).toBe(false);
    expect(
      isKnownLocation(parseEventsPath('/events/in/CH/ZG/Nirgendwo')!),
    ).toBe(false);
  });

  it('ignores trailing segments when validating', () => {
    expect(
      isKnownLocation(parseEventsPath('/events/in/CH/ZG/Zug/heute')!),
    ).toBe(true);
  });

  it('matches place names case-insensitively', () => {
    expect(isKnownLocation(parseEventsPath('/events/in/CH/zg/zug')!)).toBe(
      true,
    );
  });
});

describe('getLegacyRedirect', () => {
  const redirectFor = (pathname: string) =>
    getLegacyRedirect(parseEventsPath(pathname)!);

  it('collapses the relative date paths onto the bare place page', () => {
    for (const keyword of [
      'heute',
      'morgen',
      'gestern',
      'kommendes-wochenende',
    ]) {
      expect(redirectFor(`/events/in/CH/ZG/Zug/${keyword}`)).toBe(
        '/events/in/CH/ZG/Zug',
      );
    }
  });

  it('turns an absolute date path into a date query parameter', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/09/08')).toBe(
      '/events/in/CH/ZG/Zug?date=2026-09-08',
    );
  });

  it('pads single digit months and days', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/1/5')).toBe(
      '/events/in/CH/ZG/Zug?date=2026-01-05',
    );
  });

  it('turns an event deeplink into an event query parameter', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/heute/abc-123')).toBe(
      '/events/in/CH/ZG/Zug?event=abc-123',
    );
    expect(redirectFor('/events/in/CH/ZG/Zug/am/2026/09/08/abc-123')).toBe(
      '/events/in/CH/ZG/Zug?event=abc-123',
    );
  });

  it('re-encodes place names in the target', () => {
    expect(redirectFor('/events/in/CH/AG/Aarau%20Rohr/heute')).toBe(
      '/events/in/CH/AG/Aarau%20Rohr',
    );
  });

  it('returns null for urls that are already canonical', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug')).toBeNull();
    expect(redirectFor('/events/in/CH/ZG')).toBeNull();
    expect(redirectFor('/events/in/CH')).toBeNull();
  });

  it('returns null for an unrecognised trailing segment', () => {
    expect(redirectFor('/events/in/CH/ZG/Zug/irgendwas')).toBeNull();
  });
});
