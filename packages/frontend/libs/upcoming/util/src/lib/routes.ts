import { Params } from '@angular/router';
import dayjs, { Dayjs } from 'dayjs';
import { upperCaseStringParser } from '@feedless/core';
import { createRoutes, renderPath, str } from 'typesafe-routes';
import { cleanEventTitle } from './event-title';

export const perimeterUnit = 'Km';

export function parseDateFromQuery(queryParams: Params): {
  date: Dayjs;
  explicit: boolean;
} {
  const raw = queryParams['date'];
  if (typeof raw === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(raw)) {
    const parsed = dayjs(raw, 'YYYY-MM-DD');
    if (parsed.isValid()) {
      return { date: parsed, explicit: true };
    }
  }
  return { date: dayjs(), explicit: false };
}

export function renderPlaceUrl(
  countryCode: string,
  region: string,
  place: string,
): string {
  return renderPath(upcomingBaseRoute.events.countryCode.region.place, {
    countryCode,
    region,
    place,
  });
}

export type DateLink = {
  path: string;
  queryParams: Record<string, string>;
};

/**
 * Heute ist die nackte Ortsseite, jeder andere Tag hängt als `?date=` daran.
 * Der Canonical zeigt immer auf die parameterlose Form.
 *
 * Für `routerLink` getrennt nach Pfad und Query: bekommt `routerLink` einen
 * String mit Query-String, kodiert Angular `?` und `=` als Pfadbestandteile
 * und erzeugt `Zug%3Fdate%3D2026-09-12`.
 */
export function renderDateLink(
  countryCode: string,
  region: string,
  place: string,
  date: Dayjs | null | undefined,
): DateLink {
  const path = renderPlaceUrl(countryCode, region, place);
  if (!date || date.isSame(dayjs(), 'day')) {
    return { path, queryParams: {} };
  }
  return { path, queryParams: { date: date.format('YYYY-MM-DD') } };
}

/** Dieselbe URL als String, für `navigateByUrl` und `replaceState`. */
export function renderDateUrl(
  countryCode: string,
  region: string,
  place: string,
  date: Dayjs | null | undefined,
): string {
  const { path, queryParams } = renderDateLink(
    countryCode,
    region,
    place,
    date,
  );
  return queryParams['date'] ? `${path}?date=${queryParams['date']}` : path;
}

const UUID_AT_END =
  /([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$/i;

/**
 * `/events/in/CH/ZG/Zug/e/chilbi-baar-<uuid>`. Der Titel steht vorn, weil er
 * in Suchergebnissen und beim Teilen sichtbar ist; die id am Ende, weil sie
 * die Seite eindeutig macht. Beide enthalten Bindestriche, deshalb wird die id
 * am Ende verankert gelesen und nicht am ersten Trenner.
 */
export function renderEventUrl(
  countryCode: string,
  region: string,
  place: string,
  event: { id: string; title?: string | null },
): string {
  return renderPath(upcomingBaseRoute.events.countryCode.region.place.event, {
    countryCode,
    region,
    place,
    eventSlug: toEventSlug(cleanEventTitle(event.title), event.id),
  });
}

export function toEventSlug(
  title: string | null | undefined,
  id: string,
): string {
  const slug = (title ?? '')
    .toLowerCase()
    .replace(/ä/g, 'ae')
    .replace(/ö/g, 'oe')
    .replace(/ü/g, 'ue')
    .replace(/ß/g, 'ss')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60)
    .replace(/-+$/g, '');
  return slug ? `${slug}-${id}` : id;
}

/** Liest die id aus dem Slug. null, wenn am Ende keine uuid steht. */
export function parseEventIdFromSlug(slug: string | undefined): string | null {
  return UUID_AT_END.exec(slug ?? '')?.[1]?.toLowerCase() ?? null;
}

export const upcomingBaseRoute = createRoutes({
  terms: {
    path: ['agb'],
  },
  login: {
    path: ['login'],
  },
  about: {
    path: ['ueber-uns'],
  },
  profile: {
    path: ['profile'],
    children: {
      sources: {
        path: ['sources'],
      },
      documents: {
        path: ['documents'],
      },
      account: {
        path: ['account'],
      },
      subscriptions: {
        path: ['subscriptions'],
      },
      security: {
        path: ['security'],
      },
    },
  },
  events: {
    path: ['events', 'in'],
    children: {
      countryCode: {
        path: [upperCaseStringParser('countryCode')],
        children: {
          region: {
            path: [str('region')],
            children: {
              place: {
                path: [str('place')],
                children: {
                  event: {
                    path: ['e', str('eventSlug')],
                  },
                },
              },
            },
          },
        },
      },
    },
  },
});
