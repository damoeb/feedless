import dayjs from 'dayjs';
import { safeParsePath } from 'typesafe-routes';
import { upcomingBaseRoute } from './app/upcoming-product-routes';

export type OutdatedResult =
  | { outdated: false }
  | {
      outdated: true;
      params?: {
        day: number;
        month: number;
        year: number;
        countryCode: string;
        region: string;
        place: string;
      };
    };

export function checkOutdated(path: string): OutdatedResult {
  const parsedRoute = safeParsePath(
    upcomingBaseRoute.events.countryCode.region.place.dateTime,
    path,
  );
  if (parsedRoute.success) {
    const maxAge = dayjs().subtract(7, 'days');
    const { day, month, year, countryCode, region, place } = parsedRoute.data;
    const routeDate = dayjs()
      .year(year)
      .month(month - 1)
      .date(day);
    return {
      outdated: routeDate.isAfter(maxAge),
      params: { day, month, year, countryCode, region, place },
    };
  }

  return { outdated: false };
}
