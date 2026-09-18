import { ActivatedRouteSnapshot, ResolveFn } from '@angular/router';
import { inject } from '@angular/core';
import { Dayjs } from 'dayjs';
import { parsePath } from 'typesafe-routes';
import { AppConfigService } from '@feedless/data-access-auth';
import { NamedLatLon } from '@feedless/core';
import { AdminGeoService, GeoSearchService } from '@feedless/geo';
import { EventService, LocalizedEvent } from './event.service';
import { hasEventTitle } from '@feedless/upcoming-util';
import {
  parseDateFromQuery,
  parseEventIdFromSlug,
  upcomingBaseRoute,
} from '@feedless/upcoming-util';

export async function parseLocationFromUrl(
  activatedRoute: ActivatedRouteSnapshot,
  geoService: GeoSearchService,
): Promise<NamedLatLon> {
  const err = Error('Cannot parse location from url');

  try {
    const { place, region, countryCode } = parsePath(
      upcomingBaseRoute.events.countryCode.region.place,
      activatedRoute.params,
    );

    if (countryCode && region && place) {
      const results = await geoService.searchByObject({
        area: region,
        countryCode,
        place,
      });
      if (results.length > 0) {
        return results[0];
      }
      throw err;
    }
    throw err;
  } catch (e) {
    // If parsePath fails due to missing params, throw our custom error
    throw err;
  }
}

export type EventsResolverData = {
  events: LocalizedEvent[];
  latlng: NamedLatLon;
  date: Dayjs;
};

/**
 * Never rejects. A rejected resolver aborts the navigation, so Angular SSR
 * renders nothing, `angularApp.handle()` resolves to null and express answers
 * its default 404 - an api outage would take every relative-date url in the
 * sitemap down with it. EventCalendarPage already tolerates missing data and
 * refetches on hydration, so degrade as far as the failure requires and let
 * the page render.
 */
export const eventsResolver: ResolveFn<
  Promise<EventsResolverData | null>
> = async (route): Promise<EventsResolverData | null> => {
  const eventService = inject(EventService);
  const geoService = inject(AdminGeoService);
  const appConfigService = inject(AppConfigService);

  const { date } = parseDateFromQuery(route.queryParams);

  let latlng: NamedLatLon;
  try {
    latlng = await parseLocationFromUrl(route, geoService);
  } catch (e) {
    // Unresolvable place, or the geo backend is down - either way there is
    // nothing to anchor the page to.
    console.error('eventsResolver: cannot resolve location', e);
    return null;
  }

  const repositoryId = appConfigService.customProperties[
    'eventRepositoryId'
  ] as any;

  try {
    const events = await eventService.fetchEventsBetweenDates(
      date,
      repositoryId,
      latlng.lat,
      latlng.lng,
    );
    return { events, latlng, date };
  } catch (e) {
    // The location is known, so the page still has its heading, meta tags and
    // canonical url. Only the event list is missing.
    console.error('eventsResolver: cannot fetch events', e);
    return { events: [], latlng, date };
  }
};

export type EventDetailResolverData = {
  event: LocalizedEvent;
  place: NamedLatLon;
};

/**
 * Degradiert wie der Listen-Resolver, statt abzulehnen: eine abgelehnte
 * Navigation lässt Angular SSR nichts rendern, und express antwortet dann mit
 * seinem eigenen 404. Liefert null, wenn Ort oder Event nicht auflösbar sind -
 * die Seite entscheidet dann, was sie zeigt.
 */
export const eventDetailResolver: ResolveFn<
  Promise<EventDetailResolverData | null>
> = async (route): Promise<EventDetailResolverData | null> => {
  const eventService = inject(EventService);
  const geoService = inject(AdminGeoService);

  const eventId = parseEventIdFromSlug(route.params['eventSlug']);
  if (!eventId) {
    return null;
  }

  let place: NamedLatLon;
  try {
    place = await parseLocationFromUrl(route, geoService);
  } catch (e) {
    console.error('eventDetailResolver: cannot resolve location', e);
    return null;
  }

  const event = await eventService.findById(eventId);
  if (!event || !hasEventTitle(event)) {
    return null;
  }
  return { event, place };
};
