import { isKnownLocation, parseEventsPath } from './server-utils';

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
