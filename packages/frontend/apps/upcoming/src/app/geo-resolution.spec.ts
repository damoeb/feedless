import { AdminGeoService } from '@feedless/geo';
import {
  parseLocationFromUrl,
  upcomingBaseRoute,
} from './upcoming-product-routes';
import { ActivatedRouteSnapshot } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

/**
 * `/events/in/CH/Zürich/Hedingen/heute` used to 404: the bundled place cache
 * keys `area` by canton code only, so a canton *name* missed it and fell
 * through to public Nominatim, which blocks us. Resolution now goes to the
 * admin.ch SearchServer that the live search already uses.
 */
describe('location resolution from url', () => {
  const routeFor = (params: Record<string, string>) =>
    ({ params }) as unknown as ActivatedRouteSnapshot;

  const geoServiceReturning = (results: unknown[]) => {
    const calls: { countryCode: string; area: string; place: string }[] = [];
    return {
      calls,
      service: {
        searchByObject: async (args: any) => {
          calls.push(args);
          return results as any;
        },
        searchByQuery: async (): Promise<any[]> => [],
        reverseSearch: async () => ({}) as any,
      },
    };
  };

  it('passes the url segments through to the geo service', async () => {
    const hedingen = { lat: 47.2982564, lng: 8.4475792, place: 'Hedingen' };
    const { service, calls } = geoServiceReturning([hedingen]);

    const result = await parseLocationFromUrl(
      routeFor({ countryCode: 'CH', region: 'Zürich', place: 'Hedingen' }),
      service,
    );

    expect(calls).toEqual([
      { countryCode: 'CH', area: 'Zürich', place: 'Hedingen' },
    ]);
    expect(result).toEqual(hedingen);
  });

  it('throws when the location cannot be resolved', async () => {
    const { service } = geoServiceReturning([]);

    await expect(
      parseLocationFromUrl(
        routeFor({ countryCode: 'CH', region: 'ZH', place: 'Nowhere' }),
        service,
      ),
    ).rejects.toThrow('Cannot parse location from url');
  });

  it('queries admin.ch with the place before the area', async () => {
    // "CH Zürich Hedingen" ranks the canton first and returns Zürich;
    // "Hedingen Zürich" returns Hedingen.
    const queries: string[] = [];
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    const service = TestBed.inject(AdminGeoService);
    (service as any).searchByQuery = async (q: string): Promise<any[]> => {
      queries.push(q);
      return [];
    };

    await service.searchByObject({
      countryCode: 'CH',
      area: 'Zürich',
      place: 'Hedingen',
    });

    expect(queries).toEqual(['Hedingen Zürich']);
  });
});
