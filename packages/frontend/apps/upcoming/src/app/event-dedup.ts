import dayjs from 'dayjs';

type DedupableEvent = {
  url?: string | null;
  title?: string | null;
  startingAt?: number | null;
};

/**
 * Dieselbe Veranstaltung kommt über mehrere Quellen unter verschiedenen URLs
 * an. Gemessen am 2026-09-09: 1265 Events in 30 Tagen tragen 791 eindeutige
 * Titel, ein Duplikatanteil von 37 %. Eine Deduplizierung allein über die URL
 * fängt davon nichts.
 */
export function dedupeEvents<T extends DedupableEvent>(events: T[]): T[] {
  const seen = new Set<string>();
  return events.filter((event) => {
    const key = dedupeKey(event);
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

/**
 * Titel plus Kalendertag. Die Uhrzeit taugt nicht als Schlüssel: 96 % der
 * Events stehen auf 10:00, weil die Pipeline diesen Wert stempelt, wenn sie
 * keine Zeit erkennt.
 */
export function dedupeKey(event: DedupableEvent): string {
  const title = normalizeTitle(event.title);
  if (!title) {
    // Ohne Titel bleibt nur die URL, sonst fielen alle titellosen Einträge
    // zu einem einzigen zusammen.
    return `url:${event.url ?? ''}`;
  }
  const day = event.startingAt
    ? dayjs(event.startingAt).format('YYYY-MM-DD')
    : '';
  return `title:${title}|${day}`;
}

/**
 * Kleinschreibung, Umlaute aufgelöst, Satzzeichen und Mehrfach-Leerraum weg -
 * damit „Zämä bewegä!" und „Zämä  bewegä" denselben Schlüssel ergeben.
 */
export function normalizeTitle(title: string | null | undefined): string {
  return (title ?? '')
    .toLowerCase()
    .replace(/ä/g, 'ae')
    .replace(/ö/g, 'oe')
    .replace(/ü/g, 'ue')
    .replace(/ß/g, 'ss')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}
