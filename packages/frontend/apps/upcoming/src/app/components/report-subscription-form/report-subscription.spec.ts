import { GqlFeedlessPlugins, GqlIntervalUnit } from '@feedless/graphql-api';
import {
  SUBSCRIPTION_DISTANCE_KM,
  toSegmentInput,
} from './report-subscription';

describe('toSegmentInput', () => {
  const zug = { lat: 47.1679898, lng: 8.5173652 };
  const startingAt = new Date('2026-09-10T08:00:00Z').getTime();
  const value = { name: 'Hans Muster', email: 'hans@example.com' };

  it('addresses the recipient by name and email', () => {
    const input = toSegmentInput(value, zug, startingAt);

    expect(input.recipient.email).toEqual({
      name: 'Hans Muster',
      email: 'hans@example.com',
    });
  });

  /**
   * Das Backend kennt weiterhin mehrere Intervalle. Die Oberfläche bietet
   * keine Wahl mehr an - das Abo ist wöchentlich. Die Monatlich-Option war
   * ohnehin wirkungslos: das alte Formular erfasste sie, sendete aber immer
   * Woche.
   */
  it('always subscribes weekly', () => {
    expect(toSegmentInput(value, zug, startingAt).when.scheduled.interval).toBe(
      GqlIntervalUnit.Week,
    );
  });

  it('centres the subscription on the place of the page', () => {
    const near = toSegmentInput(value, zug, startingAt).what.latLng?.near;

    expect(near?.point).toEqual(zug);
    expect(near?.distanceKm).toBe(SUBSCRIPTION_DISTANCE_KM);
  });

  it('starts at the given moment', () => {
    expect(toSegmentInput(value, zug, startingAt).when.scheduled.startingAt).toBe(
      startingAt,
    );
  });

  it('reports through the events report plugin', () => {
    expect(toSegmentInput(value, zug, startingAt).report.plugin.pluginId).toBe(
      GqlFeedlessPlugins.OrgFeedlessEventReport,
    );
  });

  it('trims what the visitor typed', () => {
    const input = toSegmentInput(
      { name: '  Hans Muster ', email: ' hans@example.com  ' },
      zug,
      startingAt,
    );

    expect(input.recipient.email).toEqual({
      name: 'Hans Muster',
      email: 'hans@example.com',
    });
  });
});
