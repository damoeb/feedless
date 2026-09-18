import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/de';
import { NamedLatLon } from '@feedless/core';

export type DateWindowItem = {
  date: Dayjs;
  offset: number;
};

export function getDateConstraints(date: Dayjs): {
  minDate: Dayjs;
  maxDate: Dayjs;
} {
  const minDate = date;
  return {
    minDate,
    maxDate: date.add(2, 'days'),
  };
}

export function getPreviousLocations(isBrowser: boolean): NamedLatLon[] {
  if (!isBrowser) {
    return [];
  }
  let stored: unknown;
  try {
    stored = JSON.parse(localStorage.getItem('savedLocations') || '[]');
  } catch {
    return [];
  }
  return Array.isArray(stored) ? stored.filter(isSavedLocation) : [];
}

// Storage outlives app versions, so older shapes (null, lon instead of lng) still turn up.
function isSavedLocation(value: unknown): value is NamedLatLon {
  const location = value as Partial<NamedLatLon> | null;
  return (
    typeof location?.lat === 'number' &&
    typeof location.lng === 'number' &&
    (['place', 'area', 'countryCode', 'displayName'] as const).every(
      (key) => typeof location[key] === 'string',
    )
  );
}

export function getWeekday(date: Dayjs): string {
  if (date) {
    const now = dayjs()
      .set('hours', date.hour())
      .set('minutes', date.minute())
      .set('seconds', date.second())
      .set('milliseconds', date.millisecond());
    const diffInHours = date.diff(now, 'day');
    return ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'][date.day()];
  }
  return '';
}

export function formatDate(date: Dayjs, format: string) {
  return date?.locale('de')?.format(format);
}
