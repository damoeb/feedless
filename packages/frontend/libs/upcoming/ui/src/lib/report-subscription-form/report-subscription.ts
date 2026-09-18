import {
  GqlFeedlessPlugins,
  GqlIntervalUnit,
  GqlSegmentInput,
} from '@feedless/graphql-api';

/** What a visitor provides for a subscription. There's nothing else to choose today. */
export type ReportSubscriptionValue = {
  name: string;
  email: string;
};

/** Radius around the location of the page the subscription was created from. */
export const SUBSCRIPTION_DISTANCE_KM = 10;

/**
 * Translates a subscription into the input for createReport.
 *
 * Deliberately a pure function: interval, radius and plugin live in exactly
 * one place, so later editing via updateReport can reuse the same
 * translation. The interval is fixed to weekly - the backend still knows
 * several, but the UI offers no other choice.
 */
export function toSegmentInput(
  value: ReportSubscriptionValue,
  location: { lat: number; lng: number },
  startingAt: number,
): GqlSegmentInput {
  return {
    what: {
      latLng: {
        near: {
          point: { lat: location.lat, lng: location.lng },
          distanceKm: SUBSCRIPTION_DISTANCE_KM,
        },
      },
    },
    when: {
      scheduled: {
        interval: GqlIntervalUnit.Week,
        startingAt,
      },
    },
    report: {
      plugin: {
        pluginId: GqlFeedlessPlugins.OrgFeedlessEventReport,
        params: {},
      },
    },
    recipient: {
      email: {
        name: value.name.trim(),
        email: value.email.trim(),
      },
    },
  };
}
