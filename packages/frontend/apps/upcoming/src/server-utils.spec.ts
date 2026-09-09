import {
  detailMatchesPlace,
  getLegacyRedirect,
  isKnownLocation,
  isResolvableLocation,
  parseEventsPath,
  secondsUntilMidnight,
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

describe('secondsUntilMidnight', () => {
  it('counts the seconds left in the day', () => {
    expect(secondsUntilMidnight(new Date('2026-09-09T23:59:00Z'))).toBe(60);
    expect(secondsUntilMidnight(new Date('2026-09-09T00:00:00Z'))).toBe(86400);
  });

  it('never returns zero, so the header is always usable', () => {
    expect(
      secondsUntilMidnight(new Date('2026-09-09T23:59:59.999Z')),
    ).toBeGreaterThan(0);
  });
});

describe('isResolvableLocation', () => {
  const never = async () => {
    throw new Error('should not be asked');
  };

  it('accepts a cached place without asking anyone', async () => {
    expect(
      await isResolvableLocation(parseEventsPath('/events/in/CH/ZG/Zug')!, never),
    ).toBe(true);
  });

  /**
   * Die Ortsliste kennt sechs Kantone und nur deren Kürzel. Ohne diesen
   * Rückfall würde /events/in/CH/Zürich/Hedingen 404 liefern, obwohl die Seite
   * heute funktioniert - siehe geo-resolution.spec.ts.
   */
  it('asks the geo service for a place the list does not carry', async () => {
    const asked: string[] = [];
    const lookup = async (path: typeof parsed) => {
      asked.push(`${path.region}/${path.place}`);
      return true;
    };
    const parsed = parseEventsPath('/events/in/CH/Zürich/Hedingen')!;

    expect(await isResolvableLocation(parsed, lookup)).toBe(true);
    expect(asked).toEqual(['Zürich/Hedingen']);
  });

  it('rejects a place no one can resolve', async () => {
    expect(
      await isResolvableLocation(
        parseEventsPath('/events/in/CH/ZG/Nirgendwo')!,
        async () => false,
      ),
    ).toBe(false);
  });

  it('rejects an unknown hub level without asking', async () => {
    expect(
      await isResolvableLocation(parseEventsPath('/events/in/CH/QQ')!, never),
    ).toBe(false);
    expect(
      await isResolvableLocation(parseEventsPath('/events/in/XX')!, never),
    ).toBe(false);
  });

  it('lets the request through when the geo service fails', async () => {
    expect(
      await isResolvableLocation(
        parseEventsPath('/events/in/CH/BE/Bern')!,
        async () => {
          throw new Error('admin.ch down');
        },
      ),
    ).toBe(true);
  });
});

describe('detailMatchesPlace', () => {
  it('accepts a result that carries the requested place', () => {
    expect(detailMatchesPlace('Zug', 'zug zg')).toBe(true);
    expect(detailMatchesPlace('Bern', 'bern be')).toBe(true);
    expect(detailMatchesPlace('Hedingen', 'hedingen zh')).toBe(true);
  });

  it('resolves german umlauts the way the search server writes them', () => {
    expect(detailMatchesPlace('Zürich', 'zuerich zh')).toBe(true);
    expect(detailMatchesPlace('Wädenswil', 'waedenswil zh')).toBe(true);
  });

  it('also accepts a plain diacritic strip', () => {
    expect(detailMatchesPlace('Genève', 'geneve ge')).toBe(true);
  });

  it('matches a multi word place', () => {
    expect(detailMatchesPlace('Aarau Rohr', 'aarau rohr ag')).toBe(true);
    expect(detailMatchesPlace('La Chaux-de-Fonds', 'la chaux de fonds ne')).toBe(
      true,
    );
  });

  /**
   * Das ist der eigentliche Zweck: der SearchServer antwortet auf `Nirgendwo ZG`
   * mit `Zug`, auf `Quatschort XX` mit `Le Sex Blanc`.
   */
  it('rejects a fuzzy match that is a different place', () => {
    expect(detailMatchesPlace('Nirgendwo', 'zug zg')).toBe(false);
    expect(detailMatchesPlace('Quatschort', 'le sex blanc iserables')).toBe(
      false,
    );
  });

  it('rejects an empty place', () => {
    expect(detailMatchesPlace('', 'zug zg')).toBe(false);
  });
});
