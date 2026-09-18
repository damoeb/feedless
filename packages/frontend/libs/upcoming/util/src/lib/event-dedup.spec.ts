import dayjs from 'dayjs';
import { dedupeEvents, normalizeTitle } from './event-dedup';

describe('normalizeTitle', () => {
  it('folds case, umlauts, punctuation and repeated whitespace', () => {
    expect(normalizeTitle('Zämä  bewegä!')).toBe('zaemae bewegae');
    expect(normalizeTitle('Zämä bewegä')).toBe('zaemae bewegae');
  });

  it('strips accents that are not german umlauts', () => {
    expect(normalizeTitle('Fête de la Musique')).toBe('fete de la musique');
  });

  it('yields an empty string for nothing', () => {
    expect(normalizeTitle(undefined)).toBe('');
    expect(normalizeTitle('   ')).toBe('');
  });
});

describe('dedupeEvents', () => {
  const at = (day: string) => dayjs(`${day}T10:00:00`).valueOf();

  it('keeps the first of two entries that differ only in url', () => {
    const events = [
      { title: 'Chilbi Baar', url: 'https://a.example/1', startingAt: at('2026-09-12') },
      { title: 'Chilbi Baar', url: 'https://b.example/9', startingAt: at('2026-09-12') },
    ];
    expect(dedupeEvents(events).map((e) => e.url)).toEqual([
      'https://a.example/1',
    ]);
  });

  it('treats the same title on another day as another event', () => {
    const events = [
      { title: 'Wochenmarkt', url: 'https://a.example/1', startingAt: at('2026-09-12') },
      { title: 'Wochenmarkt', url: 'https://a.example/2', startingAt: at('2026-09-19') },
    ];
    expect(dedupeEvents(events)).toHaveLength(2);
  });

  /**
   * 96 % der Events stehen auf 10:00, weil die Pipeline diesen Wert stempelt,
   * wenn sie keine Zeit erkennt. Zwei Quellen, die abweichende Zeiten liefern,
   * dürfen deshalb nicht als zwei Events durchgehen.
   */
  it('ignores the time of day, only the calendar day counts', () => {
    const events = [
      { title: 'Konzert', url: 'https://a.example/1', startingAt: dayjs('2026-09-12T10:00:00').valueOf() },
      { title: 'Konzert', url: 'https://b.example/2', startingAt: dayjs('2026-09-12T20:30:00').valueOf() },
    ];
    expect(dedupeEvents(events)).toHaveLength(1);
  });

  it('does not collapse untitled entries into one', () => {
    const events = [
      { title: '', url: 'https://a.example/1', startingAt: at('2026-09-12') },
      { title: '', url: 'https://b.example/2', startingAt: at('2026-09-12') },
    ];
    expect(dedupeEvents(events)).toHaveLength(2);
  });

  it('keeps the order of what it keeps', () => {
    const events = [
      { title: 'B', url: 'u1', startingAt: at('2026-09-12') },
      { title: 'A', url: 'u2', startingAt: at('2026-09-12') },
      { title: 'B', url: 'u3', startingAt: at('2026-09-12') },
    ];
    expect(dedupeEvents(events).map((e) => e.title)).toEqual(['B', 'A']);
  });
});
