import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import { AdminGeoService } from '@feedless/geo';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { AppConfigService } from '@feedless/components';
import {
  eventsResolver,
  EventsResolverData,
} from './upcoming-product-routes';
import { EventService } from './event.service';

/**
 * A rejected resolver aborts the navigation, so Angular SSR renders nothing,
 * `angularApp.handle()` resolves to null and express answers its default 404.
 * The place page is the only indexable event url there is, so a rejecting
 * resolver would take every place in the sitemap down with it during an api
 * outage. The resolver must degrade, never reject.
 */
describe('eventsResolver', () => {
  const hedingen = {
    lat: 47.2982564,
    lng: 8.4475792,
    place: 'Hedingen',
    area: 'ZH',
    countryCode: 'CH',
    displayName: 'Hedingen',
  };

  const route = {
    params: {
      countryCode: 'CH',
      region: 'ZH',
      place: 'Hedingen',
    },
    queryParams: {},
  } as unknown as ActivatedRouteSnapshot;

  const resolve = (geo: unknown, events: unknown) => {
    TestBed.configureTestingModule({
      providers: [
        { provide: AdminGeoService, useValue: geo },
        { provide: EventService, useValue: events },
        {
          provide: AppConfigService,
          useValue: { customProperties: { eventRepositoryId: 'repo-1' } },
        },
      ],
    });
    return TestBed.runInInjectionContext(() =>
      eventsResolver(route, {} as RouterStateSnapshot),
    ) as Promise<EventsResolverData | null>;
  };

  const geoReturning = (results: unknown[]) => ({
    searchByObject: async () => results as any,
  });
  const geoFailing = () => ({
    searchByObject: async () => {
      throw new Error('Response not successful: Received status code 503');
    },
  });

  it('resolves the location, date and events', async () => {
    const data = await resolve(geoReturning([hedingen]), {
      fetchEventsBetweenDates: async () => [{ id: 'e1' }],
    });

    expect(data?.latlng).toEqual(hedingen);
    expect(data?.events).toEqual([{ id: 'e1' }]);
    expect(data?.date.isSame(new Date(), 'day')).toBe(true);
  });

  it('returns null instead of rejecting when the geo lookup fails', async () => {
    await expect(
      resolve(geoFailing(), {
        fetchEventsBetweenDates: async (): Promise<any[]> => [],
      }),
    ).resolves.toBeNull();
  });

  it('returns null instead of rejecting when the place is unknown', async () => {
    await expect(
      resolve(geoReturning([]), {
        fetchEventsBetweenDates: async (): Promise<any[]> => [],
      }),
    ).resolves.toBeNull();
  });

  it('keeps the location and renders no events when the event fetch fails', async () => {
    const data = await resolve(geoReturning([hedingen]), {
      fetchEventsBetweenDates: async () => {
        throw new Error('Response not successful: Received status code 503');
      },
    });

    expect(data?.latlng).toEqual(hedingen);
    expect(data?.events).toEqual([]);
  });
});
