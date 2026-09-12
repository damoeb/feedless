import {
  GqlFeedlessPlugins,
  GqlIntervalUnit,
  GqlSegmentInput,
} from '@feedless/graphql-api';

/** Was ein Besucher für ein Abo angibt. Mehr gibt es heute nicht zu wählen. */
export type ReportSubscriptionValue = {
  name: string;
  email: string;
};

/** Umkreis um den Ort der Seite, aus der das Abo angelegt wurde. */
export const SUBSCRIPTION_DISTANCE_KM = 10;

/**
 * Übersetzt ein Abo in die Eingabe von createReport.
 *
 * Bewusst eine reine Funktion: Intervall, Umkreis und Plugin stehen an genau
 * einer Stelle, und das spätere Bearbeiten über updateReport kann dieselbe
 * Übersetzung verwenden. Das Intervall ist fest wöchentlich - das Backend
 * kennt weiterhin mehrere, die Oberfläche bietet keine Wahl mehr an.
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
